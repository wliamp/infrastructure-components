package io.wliamp.idp.authn.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import io.wliamp.idp.authn.entity.Aud;
import io.wliamp.idp.authn.repo.AudRepo;

@Service
@RequiredArgsConstructor
public class AudService {
    private final AudRepo audRepo;

    public Flux<Aud> getAudiencesByAccountId(Long accId) {
        return audRepo.findByAccId(accId);
    }
}
