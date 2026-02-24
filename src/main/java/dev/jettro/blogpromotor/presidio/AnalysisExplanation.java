package dev.jettro.blogpromotor.presidio;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisExplanation(
        String recognizer,
        String patternName,
        String pattern,
        Double originalScore,
        Double score,
        String textualExplanation,
        Double scoreContextImprovement,
        String supportiveContextWord,
        Double validationResult
) {
}
