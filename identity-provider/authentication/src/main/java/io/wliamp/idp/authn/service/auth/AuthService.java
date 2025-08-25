package io.wliamp.idp.authn.service.auth;

import io.github.wliamp.token.util.TokenUtil;
import io.wliamp.idp.authn.service.db.AccService;
import io.wliamp.idp.authn.service.db.CacheService;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.compo.handler.CacheHandler;
import io.wliamp.idp.authn.compo.handler.TokenHandler;
import io.wliamp.idp.authn.compo.helper.PartyHelper;
import io.wliamp.idp.authn.dto.Tokens;
import io.wliamp.idp.authn.util.Generator;
import io.wliamp.idp.authn.util.Parser;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final CommonService commonService;

    private final CacheService cacheService;

    private final AccService accService;

    private final CacheHandler cacheHandler;

    private final TokenUtil tokenUtil;

    private final TokenHandler tokenHandler;

    private final Map<String, PartyHelper> loginHelpers;

    @Value("${cache.ttl.days}")
    private Duration CACHE_TTL;

    public Mono<Tokens> guestLogin() {
        String cred = "guest:" + Generator.generateCode(32);
        return commonService
                .initAccountIfNotExists(cred)
                .doOnNext(id -> log.info("created GUEST account ID = {}", id))
                .flatMap(commonService::buildScopeAndAudiencesClaims)
                .doOnNext(c -> log.debug("claims(scope, aud) built for {}", cred))
                .flatMap(claims -> tokenHandler.issueGuestToken(cred, claims))
                .doOnNext(tk -> log.info("GUEST token issued for {}", Parser.mask(cred)))
                .flatMap(tokens -> cacheHandler
                        .put("auth:" + cred, tokens, CACHE_TTL)
                        .doOnSuccess(v -> log.info("token cached for {}", Parser.mask(cred)))
                        .thenReturn(tokens));
    }

    public Mono<Tokens> loginWithoutHeader(String party, String token) {
        PartyHelper partyHelper = loginHelpers.get(party);
        return partyHelper
                .getSubject(token)
                .switchIfEmpty(
                        Mono.error(new IllegalArgumentException("Unsupported login party: " + party.toUpperCase())))
                .doOnSubscribe(s -> log.debug("start login flow for {}", party.toUpperCase()))
                .doOnNext(sub -> log.debug("extracted subject for {} -> {}", party.toUpperCase(), Parser.mask(sub)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("getSubject returned empty");
                    return Mono.error(new RuntimeException("Invalid token"));
                }))
                .map(sub -> party + ":" + sub)
                .flatMap(cred -> commonService.loginFlow(cred, partyHelper, token))
                .doOnNext(resp -> log.info(
                        "login success for {} with tokens(access, refresh) = ({}, {})",
                        party.toUpperCase(),
                        Parser.mask(resp.access()),
                        Parser.mask(resp.refresh())))
                .doOnError(err -> log.error("login failed for {}", party.toUpperCase(), err));
    }

    public Mono<Tokens> linkAccount(String party, String oldToken, String newToken) {
        return Mono.zip(tokenUtil.verify(oldToken), cacheHandler.isTokenBlacklisted(oldToken))
                .filter(tuple -> tuple.getT1() && !tuple.getT2())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid or blacklisted token")))
                .then(tokenUtil.getClaims(oldToken))
                .flatMap(claims -> Mono.justOrEmpty((String) claims.get("sub"))
                        .doOnNext(oldCred -> log.info("link account start {}", Parser.mask(oldCred)))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Missing subject in old token"))))
                .flatMap(oldCred -> Mono.justOrEmpty(loginHelpers.get(party))
                        .switchIfEmpty(Mono.error(
                                new IllegalArgumentException("Unsupported login party: " + party.toUpperCase())))
                        .flatMap(partyHelper -> {
                            String newCred = party + ":" + partyHelper.getSubject(newToken);
                            log.info("Attempt link from {} to {}", Parser.mask(newCred), Parser.mask(oldCred));
                            return accService
                                    .getAccountByCred(newCred)
                                    .flatMap(existing -> Mono.<Tokens>error(
                                            new IllegalStateException("This party account is already linked")))
                                    .switchIfEmpty(accService
                                            .updateCred(oldCred, newCred)
                                            .then(cacheHandler.evict("auth:" + oldCred))
                                            .then(commonService.initAccountIfNotExists(newCred))
                                            .flatMap(commonService::buildScopeAndAudiencesClaims)
                                            .flatMap(claimsNew ->
                                                    commonService.loginFlow(newCred, partyHelper, newToken)));
                        }));
    }

    public Mono<Tokens> loginWithHeader(String token) {
        return tokenUtil
                .verify(token)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("refresh token verification failed (invalid)");
                    return Mono.error(new IllegalArgumentException("Invalid refresh token"));
                }))
                .flatMap(v -> cacheHandler
                        .isTokenBlacklisted(token)
                        .filter(isBlacklisted -> !isBlacklisted)
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("refresh token is blacklisted");
                            return Mono.error(new IllegalStateException("Token revoked or blacklisted"));
                        }))
                        .then(tokenUtil.getClaims(token))
                        .flatMap(claims -> Mono.justOrEmpty((String) claims.get("sub"))
                                .switchIfEmpty(
                                        Mono.error(new IllegalArgumentException("Missing subject in refresh token"))))
                        .flatMap(subject -> cacheService
                                .loadTokens("auth:" + subject)
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("Session expired for {}", subject);
                                    return Mono.error(new IllegalStateException("Session expired, login required"));
                                }))
                                .flatMap(oldToken -> Mono.zip(
                                                tokenUtil.refresh(oldToken.access(), 3600),
                                                tokenUtil.refresh(token, 2592000))
                                        .flatMap(tuple -> {
                                            Tokens newTokens = new Tokens(tuple.getT1(), tuple.getT2());
                                            log.info("refreshed tokens for {}", Parser.mask(subject));
                                            return commonService
                                                    .evictAndBlacklist(subject, oldToken, token)
                                                    .then(cacheHandler.put("auth:" + subject, newTokens, CACHE_TTL))
                                                    .thenReturn(newTokens);
                                        }))));
    }

    public Mono<Void> logout(String token) {
        return tokenUtil.getClaims(token).flatMap(claims -> {
            log.info("logout requested for {}", Parser.mask(claims.get("sub")));
            return commonService.evictAndBlacklist(claims.get("sub").toString(), new Tokens(token, token), token);
        });
    }
}
