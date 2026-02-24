package dev.jettro.blogpromotor.presidio;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalyzeRequest(String text, String language, Double scoreThreshold) {

    public AnalyzeRequest(String text, String language) {
        this(text, language, null);
    }
}
