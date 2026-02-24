package dev.jettro.blogpromotor.presidio;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange
public interface PresidioAnalyzerClient {

    @PostExchange("/analyze")
    List<AnalyzeResult> analyze(@RequestBody AnalyzeRequest request);
}
