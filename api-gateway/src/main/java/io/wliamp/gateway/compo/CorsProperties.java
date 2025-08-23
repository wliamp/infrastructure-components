package io.wliamp.gateway.compo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "cors")
@Component
public class CorsProperties {
    private List<String> allowedOrigins;
}

