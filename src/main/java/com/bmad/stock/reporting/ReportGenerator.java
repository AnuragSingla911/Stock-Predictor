package com.bmad.stock.reporting;

import com.bmad.stock.shared.dto.DailyReportData;
import com.bmad.stock.shared.entity.StockPrediction;
import com.bmad.stock.shared.repository.StockPredictionRepository;
import com.bmad.stock.synthesis.RationaleSynthesisService;
import com.bmad.stock.synthesis.entity.Rationale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportGenerator {

    private final StockPredictionRepository predictionRepository;
    private final RationaleSynthesisService synthesisService;
    private final TemplateEngine templateEngine;

    @Transactional(readOnly = true)
    public String generateDailyReport(LocalDate date) {
        log.info("Generating daily report for {}", date);

        List<StockPrediction> predictions = predictionRepository.findByPredictionDate(date);
        List<DailyReportData.PredictionView> predictionViews = new ArrayList<>();

        for (StockPrediction prediction : predictions) {
            String rationaleContent = synthesisService.getRationale(prediction.getRationaleId())
                    .map(Rationale::getContent)
                    .orElse("Rationale not available.");

            predictionViews.add(new DailyReportData.PredictionView(
                prediction.getTicker().getSymbol(),
                prediction.getPredictedDirection(),
                prediction.getConfidenceScore().toString() + "%",
                rationaleContent
            ));
        }

        DailyReportData reportData = new DailyReportData(
            date.toString(),
            predictionViews,
            predictionViews.isEmpty() // Basic warning if no predictions
        );

        Context context = new Context();
        context.setVariable("report", reportData);

        return templateEngine.process("daily-report", context);
    }
}
