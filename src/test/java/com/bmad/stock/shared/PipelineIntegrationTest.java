package com.bmad.stock.shared;

import com.bmad.stock.ingestion.AlphaVantageClient;
import com.bmad.stock.ingestion.dto.AlphaVantageResponse;
import com.bmad.stock.shared.entity.StockPrediction;
import com.bmad.stock.shared.repository.StockPredictionRepository;
import com.bmad.stock.synthesis.LLMClient;
import com.bmad.stock.synthesis.repository.RationaleRepository;
import com.bmad.stock.reporting.EmailDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@ActiveProfiles("test")
public class PipelineIntegrationTest {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private StockPredictionRepository predictionRepository;

    @MockitoBean
    private AlphaVantageClient alphaVantageClient;

    @MockitoBean
    private LLMClient llmClient;

    @MockitoBean
    private RationaleRepository rationaleRepository;

    @MockitoBean
    private EmailDispatcher emailDispatcher;

    @Test
    void testFullPipelineFlow() {
        // 1. Mock Alpha Vantage Response
        AlphaVantageResponse.MetaData meta = new AlphaVantageResponse.MetaData("", "AAPL", "", "", "");
        AlphaVantageResponse.DailyData daily = new AlphaVantageResponse.DailyData("150.00", "155.00", "149.00", "152.00", "1000000");
        AlphaVantageResponse response = new AlphaVantageResponse(meta, Map.of("2026-04-26", daily));
        
        Mockito.when(alphaVantageClient.getDailyTimeSeries(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // 2. Mock LLM Response
        Map<String, Object> llmResponse = Map.of(
            "choices", List.of(
                Map.of("message", Map.of("content", "Apple showed strong resilience today with a close at 152.00."))
            )
        );
        Mockito.when(llmClient.generateChatCompletion(any())).thenReturn(llmResponse);
        Mockito.when(rationaleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // 3. Run Pipeline
        pipelineService.runDailyPipeline(List.of("AAPL"), "test@example.com");

        // 4. Verify Persistence
        List<StockPrediction> predictions = predictionRepository.findByPredictionDate(LocalDate.now());
        assertThat(predictions).isNotEmpty();
        assertThat(predictions.get(0).getTicker().getSymbol()).isEqualTo("AAPL");
    }
}
