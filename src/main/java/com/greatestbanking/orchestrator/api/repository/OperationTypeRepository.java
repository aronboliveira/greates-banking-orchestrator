package com.greatestbanking.orchestrator.api.repository;

import com.greatestbanking.orchestrator.api.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationTypeRepository extends JpaRepository<OperationType, Integer> {
}
