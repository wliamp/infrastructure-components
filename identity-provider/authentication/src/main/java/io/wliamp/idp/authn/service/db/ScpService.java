package io.wliamp.idp.authn.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import io.wliamp.idp.authn.entity.Scp;
import io.wliamp.idp.authn.repo.ScpRepo;

@Service
@RequiredArgsConstructor
public class ScpService {
    private final ScpRepo scpRepo;

    public Flux<Scp> getScopesByAccountId(Long accId) {
        return scpRepo.findByAccId(accId);
    }
}
