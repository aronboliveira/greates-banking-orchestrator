package com.pismochallenge.api.event;

import com.pismochallenge.api.entity.Account;

/**
 * In-process Spring event raised after an account is persisted.
 *
 * <p>A FIFO-queue listener consumes this in the {@code eks} profile and
 * publishes an {@code AccountEvent} message to {@code pismo-account-events.fifo}
 * with {@code MessageGroupId = ACCOUNT#<id>} for per-account ordering.
 */
public record AccountCreatedEvent(Account account) {
}
