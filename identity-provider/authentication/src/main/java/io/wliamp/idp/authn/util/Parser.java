package io.wliamp.idp.authn.util;

import java.util.List;
import java.util.stream.Collectors;
import io.wliamp.idp.authn.entity.Aud;
import io.wliamp.idp.authn.entity.Scp;

public class Parser {
    public static String parseScp(List<Scp> scps) {
        return scps.stream()
                .map(scp -> scp.getRes() + ":" + scp.getAct())
                .collect(Collectors.joining(" "));
    }

    public static List<String> parseAud(List<Aud> auds) {
        return auds.stream().map(Aud::getCode).toList();
    }

    public static String mask(Object value) {
        if (value == null) return "NULL";
        String s = value.toString();
        if (s.length() <= 8) return "****";
        return s.substring(0, 4) + "..." + s.substring(s.length() - 4);
    }
}
