package com.bmad.stock.shared.dto;

import com.bmad.stock.shared.entity.StockPrediction;
import com.bmad.stock.synthesis.entity.Rationale;
import java.util.List;

public record DailyReportData(
    String date,
    List<PredictionView> predictions,
    boolean lowConfidenceWarning
) {
    public record PredictionView(
        String symbol,
        String direction,
        String confidence,
        String rationale
    ) {}
}
