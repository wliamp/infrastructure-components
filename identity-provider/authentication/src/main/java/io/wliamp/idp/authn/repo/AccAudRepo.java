package io.wliamp.idp.authn.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import io.wliamp.idp.authn.entity.AccAud;

public interface AccAudRepo extends ReactiveCrudRepository<AccAud, Void> {}
