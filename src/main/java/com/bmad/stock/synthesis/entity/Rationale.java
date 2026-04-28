package com.bmad.stock.synthesis.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "rationales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rationale {

    @Id
    private UUID id;

    private String tickerSymbol;

    private String content;

    private Map<String, Object> metadata;

    @Builder.Default
    private Instant generatedAt = Instant.now();
}
