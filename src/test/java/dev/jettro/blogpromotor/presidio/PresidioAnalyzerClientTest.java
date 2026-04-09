package dev.jettro.blogpromotor.presidio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PresidioConfig.class)
@Import(PresidioConfig.class)
@ActiveProfiles("guardrails")
class PresidioAnalyzerClientTest {

    @Autowired
    private PresidioAnalyzerClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAnalyze() throws Exception {
        String responseJson = """
            [
              {
                "entity_type": "PERSON",
                "start": 0,
                "end": 8,
                "score": 0.95
              }
            ]
            """;

        this.server.expect(requestTo("http://localhost:5002/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("John Doe"))
                .andExpect(jsonPath("$.language").value("en"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AnalyzeRequest request = new AnalyzeRequest("John Doe", "en");
        List<AnalyzeResult> results = client.analyze(request);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().entityType()).isEqualTo("PERSON");
        assertThat(results.getFirst().start()).isEqualTo(0);
        assertThat(results.getFirst().end()).isEqualTo(8);
        assertThat(results.getFirst().score()).isEqualTo(0.95);
    }
}
