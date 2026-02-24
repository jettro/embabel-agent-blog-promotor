package dev.jettro.blogpromotor.presidio;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PatternRecognizer(
        String name,
        String supportedLanguage,
        List<Pattern> patterns,
        List<String> denyList,
        List<String> context,
        String supportedEntity
) {
}
