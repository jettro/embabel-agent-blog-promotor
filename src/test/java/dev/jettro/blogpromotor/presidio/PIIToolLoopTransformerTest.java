package dev.jettro.blogpromotor.presidio;

import com.embabel.agent.api.tool.callback.AfterLlmCallContext;
import com.embabel.agent.api.tool.callback.BeforeLlmCallContext;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PIIToolLoopTransformerTest {

    @Mock
    private PresidioAnalyzerClient presidioAnalyzerClient;

    @Mock
    private BeforeLlmCallContext beforeLlmCallContext;

    @Mock
    private AfterLlmCallContext afterLlmCallContext;

    private PIIToolLoopTransformer transformer;
    private final List<String> piiTypes = List.of("PERSON", "EMAIL_ADDRESS");

    @BeforeEach
    void setUp() {
        transformer = new PIIToolLoopTransformer(presidioAnalyzerClient, piiTypes);
    }

    @Test
    void transformBeforeLlmCall_withEmptyHistory_returnsEmptyHistory() {
        when(beforeLlmCallContext.getHistory()).thenReturn(List.of());

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).isEmpty();
        verifyNoInteractions(presidioAnalyzerClient);
    }

    @Test
    void transformBeforeLlmCall_withNoPII_returnsOriginalHistory() {
        Message message = new UserMessage("Hello, world!", "User", Instant.now());
        when(beforeLlmCallContext.getHistory()).thenReturn(List.of(message));
        when(presidioAnalyzerClient.analyze(any(AnalyzeRequest.class))).thenReturn(List.of());

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Hello, world!");
    }

    @Test
    void transformBeforeLlmCall_withPII_masksContent() {
        String originalText = "My email is john.doe@example.com and my name is John Doe.";
        Message message = new UserMessage(originalText, "User", Instant.now());
        when(beforeLlmCallContext.getHistory()).thenReturn(List.of(message));

        // Note: PIIToolLoopTransformer sorts results by start index descending to avoid shifting issues.
        // John Doe. (start 48, end 56)
        // john.doe@example.com (start 12, end 32)
        List<AnalyzeResult> analyzeResults = List.of(
                new AnalyzeResult("EMAIL_ADDRESS", 12, 32, 1.0, null),
                new AnalyzeResult("PERSON", 48, 56, 1.0, null)
        );
        when(presidioAnalyzerClient.analyze(any(AnalyzeRequest.class))).thenReturn(analyzeResults);

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).hasSize(1);
        String expectedText = "My email is <EMAIL_ADDRESS> and my name is <PERSON>.";
        assertThat(result.get(0).getContent()).isEqualTo(expectedText);
        assertThat(result.get(0).getRole().getDisplayName()).isEqualTo("User");
    }

    @Test
    void transformAfterLlmCall_returnsResponseAsIs() {
        Message response = new UserMessage("Response text", "Assistant", Instant.now());
        when(afterLlmCallContext.getResponse()).thenReturn(response);

        Message result = transformer.transformAfterLlmCall(afterLlmCallContext);

        assertThat(result).isSameAs(response);
    }
}
