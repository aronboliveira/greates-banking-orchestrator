package com.greatestbanking.orchestrator.api.repository;

import com.greatestbanking.orchestrator.api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
