package com.greatestbanking.orchestrator.api.sqs;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;

/**
 * Reads {@code PENDING} rows for the outbox publisher.
 *
 * <p>Uses {@code SKIP LOCKED} (via PostgreSQL pessimistic write lock with
 * skip-locked hint) so multiple replicas of the publisher can run in parallel
 * without contending on the same rows. {@link Pageable} caps the batch size.
 */
@Repository
@Profile("eks")
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
