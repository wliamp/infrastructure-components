package io.wliamp.idp.authn.util;

import java.util.List;
import java.util.Map;
import io.wliamp.idp.authn.entity.Aud;
import io.wliamp.idp.authn.entity.Scp;

public class Builder {
    public static Map<String, Object> buildTokenExtraClaims(List<Scp> scps, List<Aud> auds) {
        return Map.of(
                "scp", Parser.parseScp(scps),
                "aud", Parser.parseAud(auds));
    }
}
