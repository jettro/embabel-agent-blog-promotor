package dev.jettro.blogpromotor.agent;

import com.embabel.agent.api.validation.guardrails.UserInputGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;
import dev.jettro.blogpromotor.presidio.AnalyzeRequest;
import dev.jettro.blogpromotor.presidio.PresidioAnalyzerClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class implements a user input guard rail that checks for PII (Personally Identifiable Information) in user input.
 * It uses the Presidio Analyzer API to analyze the input and identify PII entities. If PII is found, it returns a validation
 * error with a level of severity of WARNING, indicating that the input contains sensitive information.
 */
public class PIIUserInputGuardRail implements UserInputGuardRail {
    private static final Logger logger = LoggerFactory.getLogger(PIIUserInputGuardRail.class);
    public static final String PII_ANALYZE_RESULT_KEY = "pii_analyze_result";

    private final PresidioAnalyzerClient presidioAnalyzerClient;
    private final List<String> piiTypes;

    public PIIUserInputGuardRail(PresidioAnalyzerClient presidioAnalyzerClient, List<String> piiTypes) {
        this.presidioAnalyzerClient = presidioAnalyzerClient;
        this.piiTypes = piiTypes;
    }


    @NotNull
    @Override
    public String getName() {
        return "PIIUserInputGuardRail";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Finds PII data in user input and rejects it.";
    }

    @NotNull
    @Override
    public ValidationResult validate(String input, @NotNull Blackboard blackboard) {
        logger.info("Validating input: {}", input);

        var request = AnalyzeRequest.builder()
                .text(input)
                .language("en")
                .entities(piiTypes)
                .build();

        var analyzeResult = presidioAnalyzerClient.analyze(request);

        blackboard.set(PII_ANALYZE_RESULT_KEY, analyzeResult);

        if (analyzeResult.isEmpty()) {
            return new ValidationResult(true, List.of());
        }

        var errors = analyzeResult.stream()
                .map(result -> {
                    var foundValue = input.substring(result.start(), result.end());
                    return new ValidationError("pii",
                            String.format("Found entity of type %s: with value '%s'", result.entityType(), foundValue),
                            ValidationSeverity.WARNING);
                })
                .toList();

        return new ValidationResult(false, errors);
    }
}
