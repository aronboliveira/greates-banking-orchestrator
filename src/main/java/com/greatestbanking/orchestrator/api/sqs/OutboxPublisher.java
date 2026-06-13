package com.greatestbanking.orchestrator.api.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Background worker that drains the outbox table to SQS.
 *
 * <p>Activated by profile {@code eks-outbox}. The bare {@code eks} profile uses
 * the lighter {@link SqsTransactionEventListener} fire-and-forget approach;
 * enable this profile <em>only</em> if zero event loss is required.
 *
 * <p>Polls every {@code gbo.outbox.fixed-delay-ms} (default 1000 ms) and
 * publishes up to {@code gbo.outbox.batch-size} (default 50) rows per tick.
 */
@Component
@Profile("eks-outbox")
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED    = "FAILED";

    private final OutboxRepository repository;
    private final SqsClient sqsClient;
    private final String queueUrl;
    private final int batchSize;

    public OutboxPublisher(OutboxRepository repository,
                           SqsClient sqsClient,
                           @Value("${aws.sqs.transaction-events-queue-url}") String queueUrl,
                           @Value("${gbo.outbox.batch-size:50}") int batchSize) {
        this.repository = repository;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${gbo.outbox.fixed-delay-ms:1000}")
    @Transactional
    public void publish() {
        List<OutboxEvent> pending = repository.findByStatusOrderByCreatedAtAsc(
            STATUS_PENDING, PageRequest.of(0, batchSize));
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxEvent event : pending) {
            try {
                sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(event.getPayload())
                    .build());
                event.setStatus(STATUS_PUBLISHED);
                event.setPublishedAt(OffsetDateTime.now());
            } catch (RuntimeException e) {
                log.error("[OUTBOX] publish failed for id={} aggregate={}: {}",
                    event.getId(), event.getAggregateId(), e.getMessage());
                event.setStatus(STATUS_FAILED);
            }
            repository.save(event);
        }
    }
}
