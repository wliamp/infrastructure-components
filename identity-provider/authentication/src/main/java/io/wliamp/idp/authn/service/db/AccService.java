package io.wliamp.idp.authn.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.wliamp.idp.authn.entity.Acc;
import io.wliamp.idp.authn.repo.AccRepo;

@Service
@RequiredArgsConstructor
public class AccService {
    private final AccRepo accRepo;

    public Mono<Acc> getAccountByCred(String cred) {
        return accRepo.findByCred(cred);
    }

    public Mono<Acc> updateCred(String oldCred, String newCred) {
        return accRepo.findByCred(oldCred).flatMap(acc -> {
            acc.setCred(newCred);
            return accRepo.save(acc);
        });
    }

    public Mono<Long> addNewAccount(String cred) {
        return accRepo.save(
                        Acc.builder().cred(cred).build())
                .map(Acc::getId);
    }
}
