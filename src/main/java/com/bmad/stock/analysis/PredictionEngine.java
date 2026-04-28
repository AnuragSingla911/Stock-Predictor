package com.bmad.stock.analysis;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.bmad.stock.shared.entity.StockPrice;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

@Service
@Slf4j
public class PredictionEngine {

    @Value("classpath:models/stock_model.onnx")
    private Resource modelResource;

    private OrtEnvironment env;
    private OrtSession session;
    private boolean modelLoaded = false;

    @PostConstruct
    public void init() {
        try {
            if (modelResource.exists()) {
                env = OrtEnvironment.getEnvironment();
                session = env.createSession(modelResource.getContentAsByteArray(), new OrtSession.SessionOptions());
                modelLoaded = true;
                log.info("ONNX model loaded successfully.");
            } else {
                log.warn("ONNX model not found at {}. PredictionEngine will run in MOCK mode.", modelResource.getDescription());
            }
        } catch (OrtException | IOException e) {
            log.error("Failed to load ONNX model. PredictionEngine will run in MOCK mode.", e);
        }
    }

    public BigDecimal predictConfidence(StockPrice price) {
        if (!modelLoaded) {
            // Mock logic: Random confidence for demo purposes
            return BigDecimal.valueOf(Math.random() * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        try {
            // Actual inference logic would go here:
            // 1. Prepare input tensor from StockPrice
            // 2. Run session
            // 3. Extract results
            log.info("Running real inference for {}", price.getTicker().getSymbol());
            return BigDecimal.valueOf(75.50); // Placeholder for real result
        } catch (Exception e) {
            log.error("Error during inference", e);
            return BigDecimal.ZERO;
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            log.error("Error closing ONNX runtime", e);
        }
    }
}
