package com.bmad.stock.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tickers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String symbol;

    private String name;

    private String sector;

    @Column(name = "is_active")
    private boolean isActive = true;
}
