package com.bmad.stock.shared.config;

import com.bmad.stock.synthesis.LLMClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class LLMConfig {

    @Value("${app.llm.base-url}")
    private String baseUrl;

    @Value("${app.llm.api-key}")
    private String apiKey;

    @Bean
    public LLMClient llmClient(RestClient.Builder builder) {
        RestClient restClient = builder
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
        
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(LLMClient.class);
    }
}
