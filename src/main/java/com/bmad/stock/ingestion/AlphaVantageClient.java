package com.bmad.stock.ingestion;

import com.bmad.stock.ingestion.dto.AlphaVantageResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.RequestParam;

@HttpExchange("/query")
public interface AlphaVantageClient {

    @GetExchange
    AlphaVantageResponse getDailyTimeSeries(
        @RequestParam("function") String function,
        @RequestParam("symbol") String symbol,
        @RequestParam("apikey") String apiKey
    );
}
