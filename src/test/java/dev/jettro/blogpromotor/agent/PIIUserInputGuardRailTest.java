package dev.jettro.blogpromotor.agent;

import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;
import dev.jettro.blogpromotor.presidio.AnalyzeRequest;
import dev.jettro.blogpromotor.presidio.AnalyzeResult;
import dev.jettro.blogpromotor.presidio.PresidioAnalyzerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static dev.jettro.blogpromotor.agent.PIIUserInputGuardRail.PII_ANALYZE_RESULT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PIIUserInputGuardRailTest {

    @Mock
    private PresidioAnalyzerClient presidioAnalyzerClient;

    @Mock
    private Blackboard blackboard;

    private PIIUserInputGuardRail guardRail;
    private final List<String> piiTypes = List.of("PERSON", "EMAIL_ADDRESS");

    @BeforeEach
    void setUp() {
        guardRail = new PIIUserInputGuardRail(presidioAnalyzerClient, piiTypes);
    }

    @Test
    void getName_returnsCorrectName() {
        assertThat(guardRail.getName()).isEqualTo("PIIUserInputGuardRail");
    }

    @Test
    void getDescription_returnsCorrectDescription() {
        assertThat(guardRail.getDescription()).isEqualTo("Finds PII data in user input and rejects it.");
    }

    @Test
    void validate_withNoPII_returnsValidResult() {
        String input = "Hello, world!";
        when(presidioAnalyzerClient.analyze(any(AnalyzeRequest.class))).thenReturn(List.of());

        ValidationResult result = guardRail.validate(input, blackboard);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        verify(blackboard).set(PII_ANALYZE_RESULT_KEY, List.of());
    }

    @Test
    void validate_withPII_returnsInvalidResultWithErrors() {
        String input = "My name is John Doe and my email is john.doe@example.com";
        // My name is John Doe and my email is john.doe@example.com
        // 01234567890123456789012345678901234567890123456789012345
        //           ^John Doe (11, 19)
        //                                     ^john.doe@example.com (36, 56)
        List<AnalyzeResult> analyzeResults = List.of(
                new AnalyzeResult("PERSON", 11, 19, 1.0, null),
                new AnalyzeResult("EMAIL_ADDRESS", 36, 56, 1.0, null)
        );
        when(presidioAnalyzerClient.analyze(any(AnalyzeRequest.class))).thenReturn(analyzeResults);

        ValidationResult result = guardRail.validate(input, blackboard);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);

        ValidationError personError = result.getErrors().getFirst();
        assertThat(personError.getCode()).isEqualTo("pii");
        assertThat(personError.getMessage()).contains("PERSON", "John Doe");
        assertThat(personError.getSeverity()).isEqualTo(ValidationSeverity.WARNING);

        ValidationError emailError = result.getErrors().get(1);
        assertThat(emailError.getCode()).isEqualTo("pii");
        assertThat(emailError.getMessage()).contains("EMAIL_ADDRESS", "john.doe@example.com");
        assertThat(emailError.getSeverity()).isEqualTo(ValidationSeverity.WARNING);

        verify(blackboard).set(PII_ANALYZE_RESULT_KEY, analyzeResults);
    }

    @Test
    void validate_callsPresidioAnalyzerWithCorrectRequest() {
        String input = "test input";
        when(presidioAnalyzerClient.analyze(any())).thenReturn(List.of());

        guardRail.validate(input, blackboard);

        ArgumentCaptor<AnalyzeRequest> captor = ArgumentCaptor.forClass(AnalyzeRequest.class);
        verify(presidioAnalyzerClient).analyze(captor.capture());

        AnalyzeRequest request = captor.getValue();
        assertThat(request.text()).isEqualTo(input);
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.entities()).isEqualTo(piiTypes);
    }
}
