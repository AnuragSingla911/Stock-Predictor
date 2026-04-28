package com.bmad.stock.shared.repository;

import com.bmad.stock.shared.entity.StockPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StockPredictionRepository extends JpaRepository<StockPrediction, UUID> {
    List<StockPrediction> findByPredictionDate(LocalDate date);
}
