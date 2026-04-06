package dev.jettro.blogpromotor.presidio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;

@SpringBootTest(classes = {
        PresidioConfig.class,
        RestClientAutoConfiguration.class,
        JacksonAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class
})
class PresidioAnalyzerIntegrationTest {

    @Autowired
    private PresidioAnalyzerClient client;

    @Autowired
    private PresidioProperties properties;

    @BeforeEach
    void setUp() {
        assumeTrue(isPresidioAvailable(), "Presidio service is not available at " + properties.baseUrl());
    }

    private boolean isPresidioAvailable() {
        try (Socket socket = new Socket()) {
            URI uri = new URI(properties.baseUrl());
            int port = uri.getPort();
            if (port == -1) {
                port = uri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
            }
            socket.connect(new InetSocketAddress(uri.getHost(), port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void testAnalyzeIntegration() {
        AnalyzeRequest request = AnalyzeRequest.builder()
                .text("My name is John Doe and my email is john.doe@example.com")
                .language("en")
                .entities(List.of("PERSON", "EMAIL_ADDRESS"))
                .build();

        List<AnalyzeResult> results = client.analyze(request);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(result -> "PERSON".equals(result.entityType()));
        assertThat(results).anyMatch(result -> "EMAIL_ADDRESS".equals(result.entityType()));
    }
}
