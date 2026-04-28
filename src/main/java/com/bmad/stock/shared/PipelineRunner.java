package com.bmad.stock.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PipelineRunner implements CommandLineRunner {

    private final PipelineService pipelineService;

    @Value("${app.pipeline.auto-start:false}")
    private boolean autoStart;

    @Value("${app.pipeline.default-symbols:AAPL,MSFT,NVDA,TSLA,GOOGL}")
    private List<String> defaultSymbols;

    @Value("${app.pipeline.test-recipient:test@example.com}")
    private String testRecipient;

    @Override
    public void run(String... args) throws Exception {
        if (autoStart) {
            log.info("Auto-starting pipeline execution as requested...");
            pipelineService.runDailyPipeline(defaultSymbols, testRecipient);
        } else {
            log.info("Pipeline auto-start is disabled. Waiting for manual trigger or schedule.");
        }
    }
}
