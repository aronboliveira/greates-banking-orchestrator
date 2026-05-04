package com.pismochallenge.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire payload for {@code pismo-account-events.fifo} SQS queue.
 */
public record AccountEventPayload(
    @JsonProperty("event_type")     String eventType,
    @JsonProperty("account_id")     Long accountId,
    @JsonProperty("published_at")   String publishedAt
) {
}
