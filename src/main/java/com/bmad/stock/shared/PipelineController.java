package com.bmad.stock.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @Value("${app.pipeline.api-key:secret}")
    private String pipelineApiKey;

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> triggerPipeline(
            @RequestHeader("X-Pipeline-Key") String apiKey,
            @RequestBody Map<String, Object> request) {
        
        if (!pipelineApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "unauthorized"));
        }

        List<String> symbols = (List<String>) request.getOrDefault("symbols", List.of("AAPL", "MSFT", "NVDA", "TSLA", "GOOGL"));
        String recipient = (String) request.getOrDefault("recipient", "user@example.com");

        // Trigger asynchronously in a real scenario
        new Thread(() -> pipelineService.runDailyPipeline(symbols, recipient)).start();

        return ResponseEntity.ok(Map.of("status", "started", "message", "Pipeline execution triggered."));
    }
}
