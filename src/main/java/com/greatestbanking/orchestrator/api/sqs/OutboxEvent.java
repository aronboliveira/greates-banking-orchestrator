package com.greatestbanking.orchestrator.api.sqs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Outbox row written in the same DB transaction as the business entity.
 *
 * <p>The {@link OutboxPublisher} polls {@code status='PENDING'} rows and ships
 * them to SQS, guaranteeing at-least-once delivery even if the pod crashes
 * between DB commit and SQS send.
 *
 * <p>Schema migration: {@code V4__outbox_events.sql}.
 *
 * <p>Note on the payload column: stored as plain {@code TEXT} (not {@code JSONB})
 * so the entity definition is portable between H2 (default profile / tests) and
 * PostgreSQL (eks profile). The Flyway V4 migration matches this — JSON content
 * is opaque to the outbox publisher; PostgreSQL's JSON-querying features are
 * not needed here.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "status", nullable = false)
    private String status;
}
