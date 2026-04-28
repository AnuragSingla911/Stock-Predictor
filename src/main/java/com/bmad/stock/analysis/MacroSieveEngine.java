package com.bmad.stock.analysis;

import com.bmad.stock.shared.entity.StockPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
public class MacroSieveEngine {

    private static final BigDecimal VOLATILITY_THRESHOLD = new BigDecimal("0.05"); // 5% daily swing

    /**
     * Determines if market conditions are suitable for prediction.
     * Simple logic: if the latest daily swing is > 5%, mark as high volatility.
     */
    public boolean isMarketStable(List<StockPrice> recentPrices) {
        if (recentPrices == null || recentPrices.isEmpty()) {
            return true;
        }

        StockPrice latest = recentPrices.get(recentPrices.size() - 1);
        BigDecimal high = latest.getHigh();
        BigDecimal low = latest.getLow();
        
        if (high == null || low == null || low.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        BigDecimal dailySwing = high.subtract(low).divide(low, 4, RoundingMode.HALF_UP);
        log.info("Daily swing for {}: {}", latest.getTicker().getSymbol(), dailySwing);

        return dailySwing.compareTo(VOLATILITY_THRESHOLD) <= 0;
    }
}
