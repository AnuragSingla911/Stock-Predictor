package com.bmad.stock.ingestion;

import com.bmad.stock.ingestion.dto.AlphaVantageResponse;
import com.bmad.stock.shared.entity.StockPrice;
import com.bmad.stock.shared.entity.Ticker;
import com.bmad.stock.shared.repository.StockPriceRepository;
import com.bmad.stock.shared.repository.TickerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataIngestionService {

    private final AlphaVantageClient client;
    private final TickerRepository tickerRepository;
    private final StockPriceRepository stockPriceRepository;
    private final String apiKey;

    public DataIngestionService(AlphaVantageClient client, 
                                TickerRepository tickerRepository,
                                StockPriceRepository stockPriceRepository,
                                @Value("${app.alphavantage.api-key}") String apiKey) {
        this.client = client;
        this.tickerRepository = tickerRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.apiKey = apiKey;
    }

    @Transactional
    @SuppressWarnings({"NullAway", "null"})
    public List<StockPrice> fetchAndSaveDailyData(String symbol) {
        log.info("Fetching daily data for symbol: {}", symbol);
        
        Ticker ticker = tickerRepository.findBySymbol(symbol)
                .orElseGet(() -> tickerRepository.save(Ticker.builder().symbol(symbol).build()));

        AlphaVantageResponse response = client.getDailyTimeSeries("TIME_SERIES_DAILY", symbol, apiKey);
        
        if (response == null || response.timeSeries() == null) {
            log.warn("No data returned for symbol: {}", symbol);
            return List.of();
        }

        List<StockPrice> savedPrices = new ArrayList<>();
        for (Map.Entry<String, AlphaVantageResponse.DailyData> entry : response.timeSeries().entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            AlphaVantageResponse.DailyData data = entry.getValue();

            // Idempotency: AlphaVantage returns overlapping history. Skip rows we've already stored.
            if (stockPriceRepository.existsByTickerAndDate(ticker, date)) {
                continue;
            }
            
            StockPrice price = StockPrice.builder()
                .ticker(ticker)
                .date(date)
                .open(new BigDecimal(data.open()))
                .high(new BigDecimal(data.high()))
                .low(new BigDecimal(data.low()))
                .close(new BigDecimal(data.close()))
                .volume(Long.parseLong(data.volume()))
                .build();
            
            // Note: In a real scenario, we might want to handle duplicates (UPSERT)
            // For MVP, we use the idx_ticker_date unique constraint in DB
            savedPrices.add(price);
        }
        
        savedPrices.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        if (savedPrices.isEmpty()) {
            // If AlphaVantage returned only overlapping history, reuse what we already have.
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30);
            List<StockPrice> existing = stockPriceRepository.findByTickerAndDateBetween(ticker, start, end);
            existing.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            if (existing.isEmpty()) {
                return List.of();
            }
            log.info("No new rows to ingest for {}. Using {} existing prices from DB.", symbol, existing.size());
            return existing;
        }
        return stockPriceRepository.saveAll(savedPrices);
    }

    /**
     * Helper to fetch data for multiple symbols with rate limiting.
     * Alpha Vantage Free Tier: 5 requests per minute.
     */
    public void fetchDataForUniverse(List<String> symbols) {
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            fetchAndSaveDailyData(symbol);
            
            // Basic rate limiting: Wait 12 seconds between requests (5 per minute)
            if (i < symbols.size() - 1) {
                try {
                    Thread.sleep(12500); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Ingestion interrupted", e);
                }
            }
        }
    }
}
