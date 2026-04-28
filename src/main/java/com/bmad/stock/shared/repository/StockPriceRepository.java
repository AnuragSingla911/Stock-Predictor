package com.bmad.stock.shared.repository;

import com.bmad.stock.shared.entity.StockPrice;
import com.bmad.stock.shared.entity.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StockPriceRepository extends JpaRepository<StockPrice, UUID> {
    List<StockPrice> findByTickerAndDateBetween(Ticker ticker, LocalDate startDate, LocalDate endDate);
    boolean existsByTickerAndDate(Ticker ticker, LocalDate date);
}
