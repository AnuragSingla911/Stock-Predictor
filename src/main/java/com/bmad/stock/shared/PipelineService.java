package com.bmad.stock.shared;

import com.bmad.stock.analysis.MacroSieveEngine;
import com.bmad.stock.analysis.PredictionEngine;
import com.bmad.stock.ingestion.DataIngestionService;
import com.bmad.stock.reporting.EmailDispatcher;
import com.bmad.stock.reporting.ReportGenerator;
import com.bmad.stock.shared.entity.StockPrediction;
import com.bmad.stock.shared.entity.StockPrice;
import com.bmad.stock.shared.repository.StockPredictionRepository;
import com.bmad.stock.synthesis.RationaleSynthesisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PipelineService {

    private final DataIngestionService ingestionService;
    private final MacroSieveEngine macroSieveEngine;
    private final PredictionEngine predictionEngine;
    private final RationaleSynthesisService synthesisService;
    private final ReportGenerator reportGenerator;
    private final EmailDispatcher emailDispatcher;
    private final StockPredictionRepository predictionRepository;

    @SuppressWarnings({"NullAway", "null"})
    public void runDailyPipeline(List<String> symbols, String recipientEmail) {
        log.info("Starting daily pipeline for {} symbols", symbols.size());
        LocalDate today = LocalDate.now();

        for (String symbol : symbols) {
            try {
                // 1. Ingestion
                List<StockPrice> prices = ingestionService.fetchAndSaveDailyData(symbol);
                if (prices == null || prices.isEmpty()) {
                    log.warn("No price data available for {}. Skipping prediction.", symbol);
                    continue;
                }
                
                // 2. Macro Sieve
                if (!macroSieveEngine.isMarketStable(prices)) {
                    log.warn("Market unstable for {}. Skipping prediction.", symbol);
                    continue;
                }

                // 3. Analysis (ML Prediction)
                StockPrice latestPrice = prices.get(0);
                var confidence = predictionEngine.predictConfidence(latestPrice);
                
                UUID rationaleId = UUID.randomUUID(); // Link ID

                StockPrediction prediction = StockPrediction.builder()
                        .ticker(latestPrice.getTicker())
                        .predictionDate(today)
                        .confidenceScore(confidence)
                        .predictedDirection("LONG") // Mock direction
                        .rationaleId(rationaleId)
                        .build();
                
                predictionRepository.save(prediction);

                // 4. Synthesis (AI Rationale)
                synthesisService.synthesizeRationale(rationaleId, symbol, latestPrice);

            } catch (Exception e) {
                log.error("Pipeline failed for symbol: {}", symbol, e);
            }
        }

        // 5. Reporting & 6. Dispatch
        try {
            String htmlReport = reportGenerator.generateDailyReport(today);
            emailDispatcher.sendReport(recipientEmail, "Daily Top 5 AI Picks - " + today, htmlReport);
            log.info("Daily pipeline completed.");
        } catch (Exception e) {
            log.error("Pipeline reporting/dispatch failed", e);
        }
    }
}
