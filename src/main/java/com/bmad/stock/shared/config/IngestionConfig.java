package com.bmad.stock.shared.config;

import com.bmad.stock.ingestion.AlphaVantageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class IngestionConfig {

    @Value("${app.alphavantage.base-url:https://www.alphavantage.co}")
    private String baseUrl;

    @Bean
    public AlphaVantageClient alphaVantageClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(baseUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AlphaVantageClient.class);
    }
}
