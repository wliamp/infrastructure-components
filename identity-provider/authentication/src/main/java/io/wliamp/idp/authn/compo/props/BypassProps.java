package io.wliamp.idp.authn.compo.props;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "security")
public class BypassProps {
    private List<String> bypassHeaders = new ArrayList<>();

    ////        Optional:
    //    private Map<String, String> bypassTokens = new HashMap<>();
}
