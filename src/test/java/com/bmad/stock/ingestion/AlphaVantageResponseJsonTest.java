package com.bmad.stock.ingestion;

import com.bmad.stock.ingestion.dto.AlphaVantageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class AlphaVantageResponseJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testDeserializeDailyTimeSeries() throws IOException {
        ClassPathResource resource = new ClassPathResource("alphavantage-daily-sample.json");
        AlphaVantageResponse response = objectMapper.readValue(resource.getInputStream(), AlphaVantageResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.metaData()).isNotNull();
        assertThat(response.metaData().symbol()).isEqualTo("IBM");
        assertThat(response.timeSeries()).isNotEmpty();
        
        String lastDate = "2026-03-20";
        assertThat(response.timeSeries()).containsKey(lastDate);
        AlphaVantageResponse.DailyData dailyData = response.timeSeries().get(lastDate);
        assertThat(dailyData.open()).isEqualTo("190.4100");
    }
}
