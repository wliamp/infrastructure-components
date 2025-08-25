package io.wliamp.idp.authn.service.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import io.wliamp.idp.authn.entity.AccScp;
import io.wliamp.idp.authn.repo.AccScpRepo;
import io.wliamp.idp.authn.repo.ScpRepo;

@Service
@RequiredArgsConstructor
public class AccScpService {
    private final AccScpRepo accScpRepo;

    private final ScpRepo scpRepo;

    public Flux<AccScp> addNewAccount(Long accId) {
        return scpRepo
                .findByStatusTrue()
                .map(scp ->
                        AccScp.builder().accId(accId).scpId(scp.getId()).build())
                .collectList()
                .flatMapMany(auds -> Flux.fromIterable(auds).flatMap(accScpRepo::save));
    }
}
