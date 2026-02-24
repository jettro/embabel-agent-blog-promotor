package dev.jettro.blogpromotor.presidio;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalyzeRequest(
        String text,
        String language,
        String correlationId,
        Double scoreThreshold,
        List<String> entities,
        Boolean returnDecisionProcess,
        List<PatternRecognizer> adHocRecognizers,
        List<String> context
) {

    public AnalyzeRequest(String text, String language) {
        this(text, language, null, null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String text;
        private String language;
        private String correlationId;
        private Double scoreThreshold;
        private List<String> entities;
        private Boolean returnDecisionProcess;
        private List<PatternRecognizer> adHocRecognizers;
        private List<String> context;

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder scoreThreshold(Double scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
            return this;
        }

        public Builder entities(List<String> entities) {
            this.entities = entities;
            return this;
        }

        public Builder returnDecisionProcess(Boolean returnDecisionProcess) {
            this.returnDecisionProcess = returnDecisionProcess;
            return this;
        }

        public Builder adHocRecognizers(List<PatternRecognizer> adHocRecognizers) {
            this.adHocRecognizers = adHocRecognizers;
            return this;
        }

        public Builder context(List<String> context) {
            this.context = context;
            return this;
        }

        public AnalyzeRequest build() {
            return new AnalyzeRequest(text, language, correlationId, scoreThreshold, entities, returnDecisionProcess, adHocRecognizers, context);
        }
    }
}
