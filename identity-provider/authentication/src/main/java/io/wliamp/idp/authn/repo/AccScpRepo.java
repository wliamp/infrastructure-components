package io.wliamp.idp.authn.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import io.wliamp.idp.authn.entity.AccScp;

public interface AccScpRepo extends ReactiveCrudRepository<AccScp, Void> {}
