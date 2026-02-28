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

public class PIIUserInputGuardRail implements UserInputGuardRail {
    private static final Logger logger = LoggerFactory.getLogger(PIIUserInputGuardRail.class);

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

        blackboard.set("pii_analyze_result", analyzeResult);

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
