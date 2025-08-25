package io.wliamp.idp.authn.service.db;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.compo.handler.CacheHandler;
import io.wliamp.idp.authn.dto.Tokens;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final CacheHandler cacheHandler;

    public Mono<Tokens> loadTokens(String key) {
        return cacheHandler.get(key, Object.class).flatMap(obj -> switch (obj) {
            case Tokens token -> Mono.just(token);
            case Map<?, ?> map -> Mono.just(new Tokens((String) map.get("access"), (String) map.get("refresh")));
            case null, default -> Mono.empty();
        });
    }
}
