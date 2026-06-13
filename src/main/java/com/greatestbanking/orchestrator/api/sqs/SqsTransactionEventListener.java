package com.greatestbanking.orchestrator.api.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatestbanking.orchestrator.api.dto.event.AccountEventPayload;
import com.greatestbanking.orchestrator.api.dto.event.TransactionEventPayload;
import com.greatestbanking.orchestrator.api.entity.Transaction;
import com.greatestbanking.orchestrator.api.event.AccountCreatedEvent;
import com.greatestbanking.orchestrator.api.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.OffsetDateTime;

/**
 * Bridges in-process domain events to SQS.
 *
 * <p>Active only in the {@code eks} profile; tests and local dev silently
 * drop the events (no listener, no SQS call).
 *
 * <p>{@link TransactionalEventListener} with {@link TransactionPhase#AFTER_COMMIT}
 * guarantees the message is only sent after the database transaction has
 * successfully committed — preventing the publication of phantom transactions
 * if the DB rolls back.
 *
 * <p>Failures are logged but do <strong>not</strong> roll back the database
 * transaction (already committed). For a strict at-least-once guarantee, use
 * the outbox pattern instead — see {@link OutboxPublisher}.
 */
@Component
@Profile("eks")
public class SqsTransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SqsTransactionEventListener.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String transactionQueueUrl;
    private final String accountQueueUrl;

    public SqsTransactionEventListener(SqsClient sqsClient,
                                       ObjectMapper objectMapper,
                                       @Value("${aws.sqs.transaction-events-queue-url}") String txUrl,
                                       @Value("${aws.sqs.account-events-queue-url:}") String acctUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.transactionQueueUrl = txUrl;
        this.accountQueueUrl = acctUrl;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        Transaction t = event.transaction();
        TransactionEventPayload payload = new TransactionEventPayload(
            "TRANSACTION_CREATED",
            t.getTransactionId(),
            t.getAccount().getAccountId(),
            t.getOperationType().getOperationTypeId(),
            t.getAmount(),
            t.getEventDate().toString(),
            OffsetDateTime.now().toString()
        );
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(transactionQueueUrl)
                .messageBody(objectMapper.writeValueAsString(payload))
                .build());
            log.debug("[SQS] tx event sent id={}", t.getTransactionId());
        } catch (JsonProcessingException e) {
            log.error("[SQS] serialization failed for tx id={}: {}", t.getTransactionId(), e.getMessage());
        } catch (RuntimeException e) {
            // Do not propagate — the DB transaction is already committed.
            // CloudWatch metric "gbo-sqs-tx-events-lag" will eventually fire if many events are dropped.
            log.error("[SQS] send failed for tx id={}: {}", t.getTransactionId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountCreated(AccountCreatedEvent event) {
        if (accountQueueUrl == null || accountQueueUrl.isBlank()) {
            return; // FIFO queue is optional
        }
        Long accountId = event.account().getAccountId();
        AccountEventPayload payload = new AccountEventPayload(
            "ACCOUNT_CREATED",
            accountId,
            OffsetDateTime.now().toString()
        );
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(accountQueueUrl)
                .messageBody(objectMapper.writeValueAsString(payload))
                .messageGroupId("ACCOUNT#" + accountId)         // FIFO ordering per account
                .messageDeduplicationId(accountId + "#CREATED") // explicit dedup ID
                .build());
            log.debug("[SQS] account event sent id={}", accountId);
        } catch (JsonProcessingException e) {
            log.error("[SQS] serialization failed for account id={}: {}", accountId, e.getMessage());
        } catch (RuntimeException e) {
            log.error("[SQS] send failed for account id={}: {}", accountId, e.getMessage());
        }
    }
}
