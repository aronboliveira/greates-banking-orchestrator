package com.pismochallenge.api.event;

import com.pismochallenge.api.entity.Transaction;

/**
 * In-process Spring application event published when a transaction is saved.
 *
 * <p>Decouples the {@code TransactionService} from any AWS-specific publishing
 * code. A profile-activated listener picks the event up and forwards it to SQS
 * (see {@code SqsTransactionEventListener}). The default profile has no
 * listener, so the event is a no-op in tests and local development.
 */
public record TransactionCreatedEvent(Transaction transaction) {
}
