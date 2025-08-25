package io.wliamp.idp.authn.compo.helper;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.compo.handler.TokenHandler;
import io.wliamp.idp.authn.dto.Tokens;

@Component("facebook")
@RequiredArgsConstructor
public class FacebookHelper implements PartyHelper {
    private final TokenHandler helper;

    public Mono<String> getSubject(String token) {
        return helper.getFacebookId(token);
    }

    @Override
    public Mono<Tokens> issueToken(String token, Map<String, Object> claims) {
        return helper.issueTokenByFacebook(token, claims);
    }
}
