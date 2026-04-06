package dev.jettro.blogpromotor.agent;

import com.embabel.agent.api.tool.callback.*;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Blackboard;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import dev.jettro.blogpromotor.presidio.AnalyzeRequest;
import dev.jettro.blogpromotor.presidio.AnalyzeResult;
import dev.jettro.blogpromotor.presidio.PresidioAnalyzerClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static dev.jettro.blogpromotor.agent.PIIUserInputGuardRail.PII_ANALYZE_RESULT_KEY;

public class PIIToolLoopTransformer implements ToolLoopTransformer {
    private static final Logger logger = LoggerFactory.getLogger(PIIToolLoopTransformer.class);

    private final PresidioAnalyzerClient presidioAnalyzerClient;
    private final List<String> piiTypes;

    public PIIToolLoopTransformer(PresidioAnalyzerClient presidioAnalyzerClient, List<String> piiTypes) {
        this.presidioAnalyzerClient = presidioAnalyzerClient;
        this.piiTypes = piiTypes;

        logger.info("PIIToolLoopTransformer initialized with piiTypes: {}", piiTypes);
    }

    @NotNull
    @Override
    public List<Message> transformBeforeLlmCall(@NotNull BeforeLlmCallContext context) {
        AgentProcess agentProcess = AgentProcess.get();
        if (agentProcess == null) {
            logger.error("AgentProcess is null, this is unexpected.");
            throw new RuntimeException("There is no reference to the agent process");
        }
        Blackboard blackboard = agentProcess.getBlackboard();

        Optional<List<AnalyzeResult>> optionalPiiAnalyzeResult = getPiiAnalyzeResult(blackboard);
        if (optionalPiiAnalyzeResult.isEmpty()) {
            return context.getHistory();
        }
        var piiAnalyzeResult = optionalPiiAnalyzeResult.get();

        var history = context.getHistory();
        logger.info("Before llm call size: {}", history.size());

        // Find the last message in the conversation and check if it is a user message
        if (history.isEmpty() || !(history.getLast() instanceof UserMessage)) {
            return history;
        }
        var lastMessage = history.getLast();

        // Replace PII entities with placeholders in the form of <ENTITY_TYPE>
        var text = lastMessage.getContent();
        StringBuilder sb = new StringBuilder(text);
        for (var result : piiAnalyzeResult) {
            sb.replace(result.start(), result.end(), "<" + result.entityType() + ">");
        }

        // Replace the message in the history with the transformed one
        Message transformedMessage = new UserMessage(sb.toString(), lastMessage.getRole().getDisplayName(),
                lastMessage.getTimestamp());
        history.set(history.size() - 1, transformedMessage);

        logger.info("After transformation: {}", transformedMessage.getContent());

        return history;
    }

    @NotNull
    @Override
    public Message transformAfterLlmCall(@NotNull AfterLlmCallContext context) {
        var response = context.getResponse();
        logger.info("After llm call response: {}", context.getResponse().getContent());
        return response;
    }

    @NotNull
    @Override
    public String transformAfterToolResult(@NotNull AfterToolResultContext context) {
        return ToolLoopTransformer.super.transformAfterToolResult(context);
    }

    @NotNull
    @Override
    public List<Message> transformAfterIteration(@NotNull AfterIterationContext context) {
        return ToolLoopTransformer.super.transformAfterIteration(context);
    }

    private Optional<List<AnalyzeResult>> getPiiAnalyzeResult(Blackboard blackboard) {
        Object piiAnalyzeResult = blackboard.get(PII_ANALYZE_RESULT_KEY);
        // Use pattern matching, for instanceof to safely cast and check for null
        if (!(piiAnalyzeResult instanceof List<?> rawList)) {
            logger.info("No PII analyze result found or it is not a list.");
            return Optional.empty();
        }
        // Ensure the list contains the expected types (optional but recommended for safety)
        List<AnalyzeResult> analyzeResultList = rawList.stream()
                .filter(AnalyzeResult.class::isInstance)
                .map(AnalyzeResult.class::cast)
                .toList();

        if (analyzeResultList.isEmpty()) {
            return Optional.empty();
        }

        // Log the found PII entities
        analyzeResultList.forEach(result ->
                logger.info("Entity: {}, Start: {}, End: {}", result.entityType(), result.start(), result.end())
        );

        return Optional.of(analyzeResultList);
    }
}
