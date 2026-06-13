package com.greatestbanking.orchestrator.api.repository;

import com.greatestbanking.orchestrator.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
