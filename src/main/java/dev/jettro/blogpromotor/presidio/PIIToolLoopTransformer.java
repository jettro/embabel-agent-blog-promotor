package dev.jettro.blogpromotor.presidio;

import com.embabel.agent.api.tool.callback.*;
import com.embabel.chat.Message;
import com.embabel.chat.UserMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

public class PIIToolLoopTransformer implements ToolLoopTransformer {
    private static final Logger logger = LoggerFactory.getLogger(PIIToolLoopTransformer.class);

    private final PresidioAnalyzerClient presidioAnalyzerClient;
    private final List<String> piiTypes;

    public PIIToolLoopTransformer(PresidioAnalyzerClient presidioAnalyzerClient, List<String> piiTypes) {
        this.presidioAnalyzerClient = presidioAnalyzerClient;
        this.piiTypes = piiTypes;
    }

    @NotNull
    @Override
    public List<Message> transformBeforeLlmCall(@NotNull BeforeLlmCallContext context) {
        var history = context.getHistory();
        logger.info("Before llm call size: {}", history.size());

        // Find the last message in the conversation and check it it is a user message
        var lastMessage = history.getLast();
        if (history.isEmpty() || !(lastMessage instanceof UserMessage)) {
            return history;
        }

        logger.info("Last message: {}", lastMessage.getContent());
        var request = AnalyzeRequest.builder()
                .text(lastMessage.getContent())
                .language("en")
                .entities(piiTypes)
                .build();

        var analyzeResult = presidioAnalyzerClient.analyze(request);

        if (analyzeResult == null || analyzeResult.isEmpty()) {
            return history;
        }

        // Sort results in descending order by start index to avoid shifting issues during replacement
        var sortedResults = analyzeResult.stream()
                .sorted(Comparator.comparingInt(AnalyzeResult::start).reversed())
                .toList();

        // Replace PII entities with placeholders in the form of <ENTITY_TYPE>
        var text = lastMessage.getContent();
        StringBuilder sb = new StringBuilder(text);
        for (var result : sortedResults) {
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
}
