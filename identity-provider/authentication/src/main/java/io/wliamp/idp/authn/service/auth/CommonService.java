package io.wliamp.idp.authn.service.auth;

import io.github.wliamp.token.util.TokenUtil;
import io.wliamp.idp.authn.service.db.*;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.compo.handler.CacheHandler;
import io.wliamp.idp.authn.compo.helper.PartyHelper;
import io.wliamp.idp.authn.dto.Tokens;
import io.wliamp.idp.authn.entity.Acc;
import io.wliamp.idp.authn.util.Builder;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonService {
    private final AccService accService;

    private final AudService audService;

    private final ScpService scpService;

    private final AccAudService accAudService;

    private final AccScpService accScpService;

    private final TokenUtil tokenUtil;

    private final CacheHandler cacheHandler;

    @Value("${cache.ttl.days}")
    private Duration CACHE_TTL;

    public Mono<Long> initAccountIfNotExists(String cred) {
        return accService
                .getAccountByCred(cred)
                .doOnNext(acc -> log.info("Found existing account for {}", cred))
                .map(Acc::getId)
                .switchIfEmpty(accService
                        .addNewAccount(cred)
                        .flatMap(accId -> Mono.when(
                                        accScpService.addNewAccount(accId), accAudService.addNewAccount(accId))
                                .thenReturn(accId))
                        .doOnNext(newId -> log.info("Created new account {} for {}", newId, cred)));
    }

    public Mono<Map<String, Object>> buildScopeAndAudiencesClaims(Long accId) {
        return Mono.zip(
                        scpService.getScopesByAccountId(accId).collectList(),
                        audService.getAudiencesByAccountId(accId).collectList())
                .map(t -> Builder.buildTokenExtraClaims(t.getT1(), t.getT2()));
    }

    public Mono<Tokens> loginFlow(String cred, PartyHelper partyHelper, String token) {
        return initAccountIfNotExists(cred)
                .doOnNext(id -> log.info("AccountId = {}", id))
                .flatMap(this::buildScopeAndAudiencesClaims)
                .doOnNext(c -> log.info("Claims built"))
                .flatMap(claims -> partyHelper.issueToken(token, claims))
                .doOnNext(tk -> log.info("Token issued"))
                .flatMap(userToken ->
                        cacheHandler.put("auth:" + cred, userToken, CACHE_TTL).thenReturn(userToken));
    }

    public Mono<Void> evictAndBlacklist(String subject, Tokens oldToken, String refreshToken) {
        long now = System.currentTimeMillis() / 1000;
        long accessExp = getExpClaim(oldToken.access());
        long accessTTL = Math.max(0, accessExp - now);
        long refreshExp = getExpClaim(refreshToken);
        long refreshTTL = Math.max(0, refreshExp - now);
        return cacheHandler
                .evict("auth:" + subject)
                .then(cacheHandler.blacklistToken(oldToken.access(), Duration.ofSeconds(accessTTL)))
                .then(cacheHandler.blacklistToken(refreshToken, Duration.ofSeconds(refreshTTL)))
                .then();
    }

    public long getExpClaim(String token) {
        try {
            Map<String, Object> claims = tokenUtil.getClaims(token).block();
            assert claims != null;
            Object expObj = claims.get("exp");
            return expObj != null ? Long.parseLong(expObj.toString()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
