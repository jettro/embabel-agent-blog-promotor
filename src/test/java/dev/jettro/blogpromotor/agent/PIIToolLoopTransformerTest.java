package dev.jettro.blogpromotor.agent;

import com.embabel.agent.api.tool.callback.AfterLlmCallContext;
import com.embabel.agent.api.tool.callback.BeforeLlmCallContext;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Blackboard;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import dev.jettro.blogpromotor.presidio.AnalyzeResult;
import dev.jettro.blogpromotor.presidio.PresidioAnalyzerClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static dev.jettro.blogpromotor.agent.PIIUserInputGuardRail.PII_ANALYZE_RESULT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PIIToolLoopTransformerTest {

    @Mock
    private BeforeLlmCallContext beforeLlmCallContext;

    @Mock
    private AfterLlmCallContext afterLlmCallContext;

    @Mock
    private Blackboard blackboard;

    @Mock
    private AgentProcess agentProcess;

    private PIIToolLoopTransformer transformer;
    private MockedStatic<AgentProcess> mockedAgentProcess;

    @BeforeEach
    void setUp() {
        mockedAgentProcess = mockStatic(AgentProcess.class);
        mockedAgentProcess.when(AgentProcess::get).thenReturn(agentProcess);
        lenient().when(agentProcess.getBlackboard()).thenReturn(blackboard);

        transformer = new PIIToolLoopTransformer();
    }

    @AfterEach
    void tearDown() {
        mockedAgentProcess.close();
    }

    @Test
    void transformBeforeLlmCall_withEmptyHistory_returnsEmptyHistory() {
        when(beforeLlmCallContext.getHistory()).thenReturn(List.of());
        when(blackboard.get(PII_ANALYZE_RESULT_KEY)).thenReturn(List.of());

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).isEmpty();
    }

    @Test
    void transformBeforeLlmCall_withNoPII_returnsOriginalHistory() {
        Message message = new UserMessage("Hello, world!", "User", Instant.now());
        when(beforeLlmCallContext.getHistory()).thenReturn(List.of(message));
        when(blackboard.get(PII_ANALYZE_RESULT_KEY)).thenReturn(List.of());

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Hello, world!");
    }

    @Test
    void transformBeforeLlmCall_withPII_masksContent() {
        String originalText = "My email is john.doe@example.com and my name is John Doe.";
        Message message = new UserMessage(originalText, "User", Instant.now());
        when(beforeLlmCallContext.getHistory()).thenReturn(new java.util.ArrayList<>(List.of(message)));

        // Note: PIIToolLoopTransformer sorts results by start index descending to avoid shifting issues.
        // My email is john.doe@example.com and my name is John Doe.
        // john.doe@example.com (start 12, end 32)
        // John Doe. (start 48, end 56)
        List<AnalyzeResult> analyzeResults = List.of(
                new AnalyzeResult("EMAIL_ADDRESS", 12, 32, 1.0, null),
                new AnalyzeResult("PERSON", 48, 56, 1.0, null)
        );
        when(blackboard.get(PII_ANALYZE_RESULT_KEY)).thenReturn(analyzeResults);

        List<Message> result = transformer.transformBeforeLlmCall(beforeLlmCallContext);

        assertThat(result).hasSize(1);
        String expectedText = "My email is <EMAIL_ADDRESS> and my name is <PERSON>.";
        assertThat(result.getFirst().getContent()).isEqualTo(expectedText);
        assertThat(result.getFirst().getRole().getDisplayName()).isEqualTo("User");
    }

    @Test
    void transformAfterLlmCall_returnsResponseAsIs() {
        Message response = new UserMessage("Response text", "Assistant", Instant.now());
        when(afterLlmCallContext.getResponse()).thenReturn(response);

        Message result = transformer.transformAfterLlmCall(afterLlmCallContext);

        assertThat(result).isSameAs(response);
    }
}
