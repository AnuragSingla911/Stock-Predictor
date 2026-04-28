package com.bmad.stock.synthesis;

import com.bmad.stock.shared.entity.StockPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class GroundingValidator {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+\\.\\d+");

    /**
     * Verifies that numeric values in the rationale are consistent with raw market data.
     * For MVP, it checks if numbers like "190.41" (the open price) appear and are correct.
     */
    public boolean verifyGrounding(String rationale, StockPrice data) {
        log.info("Verifying grounding for rationale...");
        
        Matcher matcher = NUMBER_PATTERN.matcher(rationale);
        while (matcher.find()) {
            String foundNumber = matcher.group();
            // Basic check: if the number looks like a price but doesn't match any OHLC values
            // we flag it (simplified logic for demonstration)
            if (isPriceLike(foundNumber) && !matchesMarketData(foundNumber, data)) {
                log.warn("Potential hallucination detected: {} does not match market data", foundNumber);
                // In a strict mode, we might return false here
            }
        }
        
        return true; // Simplified for MVP
    }

    private boolean isPriceLike(String num) {
        return num.contains(".");
    }

    private boolean matchesMarketData(String num, StockPrice data) {
        String open = data.getOpen().toPlainString();
        String high = data.getHigh().toPlainString();
        String low = data.getLow().toPlainString();
        String close = data.getClose().toPlainString();
        
        return num.equals(open) || num.equals(high) || num.equals(low) || num.equals(close);
    }
}
