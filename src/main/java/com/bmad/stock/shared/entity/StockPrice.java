package com.bmad.stock.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_prices", indexes = {
    @Index(name = "idx_ticker_date", columnList = "ticker_id, date", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_id", nullable = false)
    private Ticker ticker;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 19, scale = 4)
    private BigDecimal open;

    @Column(precision = 19, scale = 4)
    private BigDecimal high;

    @Column(precision = 19, scale = 4)
    private BigDecimal low;

    @Column(precision = 19, scale = 4)
    private BigDecimal close;

    private Long volume;
}
