package io.wliamp.idp.authn.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import io.wliamp.idp.authn.entity.AccScope;

public interface AccScopeRepo extends ReactiveCrudRepository<AccScope, Void> {}
