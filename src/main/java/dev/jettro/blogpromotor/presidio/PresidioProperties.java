package dev.jettro.blogpromotor.presidio;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "presidio.analyzer")
public record PresidioProperties(String baseUrl) {
}
