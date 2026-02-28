package dev.jettro.blogpromotor.presidio;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "presidio.analyzer")
public record PresidioProperties(
        String baseUrl,
        List<String> piiTypes
) {
}
