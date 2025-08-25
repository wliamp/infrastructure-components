package io.wliamp.idp.authn.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import io.wliamp.idp.authn.entity.AccAud;
import io.wliamp.idp.authn.repo.AccAudRepo;
import io.wliamp.idp.authn.repo.AudRepo;

@Service
@RequiredArgsConstructor
public class AccAudService {
    private final AccAudRepo accAudRepo;

    private final AudRepo audRepo;

    public Flux<AccAud> addNewAccount(Long accId) {
        return audRepo.findByStatusTrue()
                .map(aud -> AccAud.builder().accId(accId).audId(aud.getId()).build())
                .collectList()
                .flatMapMany(auds -> Flux.fromIterable(auds).flatMap(accAudRepo::save));
    }
}
