package io.wliamp.idp.authn.compo.helper;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.compo.handler.TokenHandler;
import io.wliamp.idp.authn.dto.Tokens;

@Component("zalo")
@RequiredArgsConstructor
public class ZaloHelper implements PartyHelper {
    private final TokenHandler helper;

    @Override
    public Mono<String> getSubject(String token) {
        return helper.getZaloId(token);
    }

    @Override
    public Mono<Tokens> issueToken(String token, Map<String, Object> claims) {
        return helper.issueTokenByZalo(token, claims);
    }
}
