package io.wliamp.idp.authn.compo.helper;

import java.util.Map;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.dto.Tokens;

public interface PartyHelper {
    Mono<String> getSubject(String token);

    Mono<Tokens> issueToken(String token, Map<String, Object> claims);
}
