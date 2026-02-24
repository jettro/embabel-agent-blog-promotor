package dev.jettro.blogpromotor.presidio;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeRequestTest {

    @Test
    void testAnalyzeRequestConstructor() {
        AnalyzeRequest request = new AnalyzeRequest("Hello world", "en");
        assertThat(request.text()).isEqualTo("Hello world");
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.entities()).isNull();
    }

    @Test
    void testAnalyzeRequestBuilder() {
        AnalyzeRequest request = AnalyzeRequest.builder()
                .text("Hello world")
                .language("en")
                .entities(List.of("PERSON"))
                .scoreThreshold(0.5)
                .build();
        assertThat(request.text()).isEqualTo("Hello world");
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.entities()).containsExactly("PERSON");
        assertThat(request.scoreThreshold()).isEqualTo(0.5);
    }
}
