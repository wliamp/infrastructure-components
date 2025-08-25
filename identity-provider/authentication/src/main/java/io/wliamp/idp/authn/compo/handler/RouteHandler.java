package io.wliamp.idp.authn.compo.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.service.authenticate.AuthService;

@Component
@RequiredArgsConstructor
public class RouteHandler {
    private final AuthService authService;

    private final ResponseHandler responseHandler;

    public Mono<ServerResponse> guest(ServerRequest request) {
        return authService.guestLogin().flatMap(responseHandler::buildTokenResponse);
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        String party = request.pathVariable("party");
        return request.bodyToMono(String.class)
                .flatMap(external -> authService.loginWithoutHeader(party, external))
                .flatMap(responseHandler::buildTokenResponse);
    }

    public Mono<ServerResponse> relog(ServerRequest request) {
        String token = request.headers().firstHeader("X-Refresh-Token");
        if (token == null) {
            return ServerResponse.badRequest().build();
        }
        return authService.loginWithHeader(token).flatMap(responseHandler::buildTokenResponse);
    }

    public Mono<ServerResponse> link(ServerRequest request) {
        String oldToken = request.headers().firstHeader("X-Refresh-Token");
        String party = request.pathVariable("party");
        return request.bodyToMono(String.class)
                .flatMap(newToken -> {
                    assert oldToken != null;
                    return authService.linkAccount(party, oldToken, newToken);
                })
                .flatMap(responseHandler::buildTokenResponse);
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        String token = request.headers().firstHeader("X-Refresh-Token");
        assert token != null;
        return authService.logout(token).then(ServerResponse.ok().build());
    }
}
