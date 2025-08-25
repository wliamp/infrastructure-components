package io.wliamp.idp.authn.compo.handler;

import io.wliamp.idp.authn.dto.Tokens;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ResponseHandler {
    public Mono<ServerResponse> buildTokenResponse(Tokens token) {
        return ServerResponse.ok()
                .header("X-Access-Token", token.access())
                .header("X-Refresh-Token", token.refresh())
                .build();
    }
}
