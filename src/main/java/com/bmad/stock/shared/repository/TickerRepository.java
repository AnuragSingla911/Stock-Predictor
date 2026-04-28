package com.bmad.stock.shared.repository;

import com.bmad.stock.shared.entity.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TickerRepository extends JpaRepository<Ticker, UUID> {
    Optional<Ticker> findBySymbol(String symbol);
}
