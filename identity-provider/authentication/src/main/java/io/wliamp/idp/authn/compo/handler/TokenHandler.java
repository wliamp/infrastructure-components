package io.wliamp.idp.authn.compo.handler;

import java.util.Map;

import io.github.wliamp.provider.util.OauthVerifier;
import io.github.wliamp.token.data.Type;
import io.github.wliamp.token.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.dto.Tokens;

@Component
@RequiredArgsConstructor
public class TokenHandler {
    private final TokenUtil tokenUtil;

    private final OauthVerifier oauthVerifier;

    private Mono<Map<String, Object>> getFacebookInfo(String token) {
        return oauthVerifier
                .getFacebook()
                .verify(token)
                .flatMap(valid -> valid ? oauthVerifier.getFacebook().getInfo(token) : Mono.empty());
    }

    private Mono<Map<String, Object>> getGoogleInfo(String token) {
        return oauthVerifier
                .getGoogle()
                .verify(token)
                .flatMap(valid -> valid ? oauthVerifier.getGoogle().getInfo(token) : Mono.empty());
    }

    private Mono<Map<String, Object>> getZaloInfo(String token) {
        return oauthVerifier
                .getZalo()
                .verify(token)
                .flatMap(valid -> valid ? oauthVerifier.getZalo().getInfo(token) : Mono.empty());
    }

    public Mono<Tokens> issueGuestToken(String sub, Map<String, Object> extraClaims) {
        Mono<String> accessMono = tokenUtil.issue(sub, Type.ACCESS, 3600, extraClaims);
        Mono<String> refreshMono = tokenUtil.issue(sub, Type.REFRESH, 2592000);
        return Mono.zip(accessMono, refreshMono).map(tuple -> new Tokens(tuple.getT1(), tuple.getT2()));
    }

    public Mono<String> getFacebookId(String token) {
        return getFacebookInfo(token).map(info -> info.get("id").toString());
    }

    public Mono<String> getGoogleId(String token) {
        return getGoogleInfo(token).map(info -> info.get("sub").toString());
    }

    public Mono<String> getZaloId(String token) {
        return getZaloInfo(token).map(info -> info.get("id").toString());
    }

    public Mono<Tokens> issueTokenByFacebook(String token, Map<String, Object> extraClaims) {
        return getFacebookId(token).map(id -> "facebook:" + id).flatMap(cred -> issueGuestToken(cred, extraClaims));
    }

    public Mono<Tokens> issueTokenByGoogle(String token, Map<String, Object> extraClaims) {
        return getGoogleId(token).map(id -> "google:" + id).flatMap(cred -> issueGuestToken(cred, extraClaims));
    }

    public Mono<Tokens> issueTokenByZalo(String token, Map<String, Object> extraClaims) {
        return getZaloId(token).map(id -> "zalo:" + id).flatMap(cred -> issueGuestToken(cred, extraClaims));
    }
}
