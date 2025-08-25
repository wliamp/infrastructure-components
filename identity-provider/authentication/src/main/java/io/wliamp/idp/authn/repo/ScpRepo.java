package io.wliamp.idp.authn.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import io.wliamp.idp.authn.entity.Scp;

public interface ScpRepo extends ReactiveCrudRepository<Scp, Long> {
    //    @Query("""
    //            SELECT *
    //            FROM scopes
    //            WHERE status = true
    //            """)
    Flux<Scp> findByStatusTrue();

    @Query(
            """
            SELECT s.*
            FROM scopes s
            JOIN account_scope sa ON s.id = sa.scope_id
            JOIN accounts a ON sa.account_id = a.id
            WHERE a.id = :accId
            """)
    Flux<Scp> findByAccId(@Param("accId") Long accId);
}
