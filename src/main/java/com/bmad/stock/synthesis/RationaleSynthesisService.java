package com.bmad.stock.synthesis;

import com.bmad.stock.shared.entity.StockPrice;
import com.bmad.stock.synthesis.entity.Rationale;
import com.bmad.stock.synthesis.repository.RationaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RationaleSynthesisService {

    private final RationaleRepository rationaleRepository;
    private final LLMClient llmClient;
    private final GroundingValidator groundingValidator;

    @Value("classpath:prompts/stock_rationale.prompt")
    private Resource promptTemplate;

    @Value("${app.llm.model:gpt-4-turbo}")
    private String modelName;

    @Value("${app.llm.api-key:none}")
    private String llmApiKey;

    @SuppressWarnings({"NullAway", "null"})
    public Rationale synthesizeRationale(UUID predictionId, String symbol, StockPrice marketData) {
        log.info("Synthesizing rationale for symbol: {}", symbol);

        try {
            if (llmApiKey == null || llmApiKey.isBlank() || "none".equalsIgnoreCase(llmApiKey)) {
                log.warn("LLM API key not configured. Skipping rationale synthesis for {}", symbol);
                Rationale rationale = Rationale.builder()
                        .id(predictionId)
                        .tickerSymbol(symbol)
                        .content("Rationale synthesis skipped: LLM API key not configured.")
                        .metadata(Map.of("model", modelName, "grounded", false, "skipped", true))
                        .build();
                return rationaleRepository.save(rationale);
            }

            String prompt = loadPrompt(symbol, marketData);
            
            Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", List.of(
                    Map.of("role", "system", "content", "You are a professional financial analyst. Provide scannable, data-driven stock rationales."),
                    Map.of("role", "user", "content", prompt)
                )
            );

            Map<String, Object> response = llmClient.generateChatCompletion(request);
            String content = extractContent(response);

            // Grounding Verification
            if (!groundingValidator.verifyGrounding(content, marketData)) {
                log.warn("Rationale grounding failed for {}. Using fallback or flagging.", symbol);
                content = "Warning: Rationale could not be fully verified against raw data.\n\n" + content;
            }

            Rationale rationale = Rationale.builder()
                .id(predictionId)
                .tickerSymbol(symbol)
                .content(content)
                .metadata(Map.of("model", modelName, "grounded", true))
                .build();

            return rationaleRepository.save(rationale);

        } catch (Exception e) {
            log.error("Failed to synthesize rationale", e);
            // Don't fail the pipeline if the LLM call fails; persist a fallback rationale.
            Rationale fallback = Rationale.builder()
                    .id(predictionId)
                    .tickerSymbol(symbol)
                    .content("Rationale synthesis failed. Please try again later.")
                    .metadata(Map.of("model", modelName, "grounded", false, "error", e.getClass().getSimpleName()))
                    .build();
            return rationaleRepository.save(fallback);
        }
    }

    private String loadPrompt(String symbol, StockPrice data) throws IOException {
        @SuppressWarnings({"NullAway", "null"})
        String template = promptTemplate.getContentAsString(StandardCharsets.UTF_8);
        return template
            .replace("{{symbol}}", symbol)
            .replace("{{open}}", data.getOpen().toPlainString())
            .replace("{{high}}", data.getHigh().toPlainString())
            .replace("{{low}}", data.getLow().toPlainString())
            .replace("{{close}}", data.getClose().toPlainString())
            .replace("{{volume}}", String.valueOf(data.getVolume()));
    }

    private String extractContent(Map<String, Object> response) {
        // Simplified extraction from OpenAI-style response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    @SuppressWarnings({"NullAway", "null"})
    public Optional<Rationale> getRationale(UUID id) {
        return rationaleRepository.findById(id);
    }
}
