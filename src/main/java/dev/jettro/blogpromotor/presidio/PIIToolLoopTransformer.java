package dev.jettro.blogpromotor.presidio;

import com.embabel.agent.api.tool.callback.*;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Blackboard;
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
    private final Blackboard blackboard;

    public PIIToolLoopTransformer(PresidioAnalyzerClient presidioAnalyzerClient, List<String> piiTypes, Blackboard blackboard) {
        this.presidioAnalyzerClient = presidioAnalyzerClient;
        this.piiTypes = piiTypes;
        this.blackboard = blackboard;

        logger.info("PIIToolLoopTransformer initialized with piiTypes: {}", piiTypes);
    }

    @NotNull
    @Override
    public List<Message> transformBeforeLlmCall(@NotNull BeforeLlmCallContext context) {
        logger.info("HELP: Transformer thread: {}", Thread.currentThread().getName());
        logger.info("Transforming before llm call");
        logger.info("There is a Thread local for the agent process: {}", AgentProcess.get() != null);
        AgentProcess agentProcess = AgentProcess.get();
        if (agentProcess != null) {
            Blackboard blackboard = agentProcess.getBlackboard();
            blackboard.getObjects().forEach(o -> logger.info("Object on blackboard: {}", o.getClass().getSimpleName()));
        } else {
            logger.error("AgentProcess is null");
            this.blackboard.getObjects().forEach(o -> logger.info("Object on blackboard: {}", o.getClass().getSimpleName()));

            Object piiAnalyzeResult = this.blackboard.get("pii_analyze_result");
            
            if (piiAnalyzeResult != null && List.class.isAssignableFrom(piiAnalyzeResult.getClass())) {
                logger.info("PII analyze result is a list");
                List<AnalyzeResult> analyzeResultList = (List<AnalyzeResult>) piiAnalyzeResult;
                analyzeResultList.forEach(result -> logger.info("Entity: {}, Start: {}, End: {}", result.entityType(), result.start(), result.end()));
            }
        }

        var history = context.getHistory();
        logger.info("Before llm call size: {}", history.size());

        // Find the last message in the conversation and check it it is a user message
        if (history.isEmpty()) {
            return history;
        }
        var lastMessage = history.getLast();
        if (!(lastMessage instanceof UserMessage)) {
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
