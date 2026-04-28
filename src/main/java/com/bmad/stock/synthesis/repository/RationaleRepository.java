package com.bmad.stock.synthesis.repository;

import com.bmad.stock.synthesis.entity.Rationale;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;

public interface RationaleRepository extends MongoRepository<Rationale, UUID> {
}
