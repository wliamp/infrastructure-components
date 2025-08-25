package io.wliamp.idp.authn.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.entity.Acc;

public interface AccRepo extends ReactiveCrudRepository<Acc, Long> {
    Mono<Acc> findByCred(String cred);
}
