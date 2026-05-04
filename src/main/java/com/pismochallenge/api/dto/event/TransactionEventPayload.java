package com.pismochallenge.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Wire payload for {@code pismo-transaction-events} SQS queue.
 *
 * <p>JSON schema is defined in
 * {@code .tmp/claude/2026-05-04/aws-policies-and-manifests.json} → {@code sqs.message_schema}.
 * Snake-case JSON property names match the Lambda consumer's expected schema.
 */
public record TransactionEventPayload(
    @JsonProperty("event_type")        String eventType,
    @JsonProperty("transaction_id")    Long transactionId,
    @JsonProperty("account_id")        Long accountId,
    @JsonProperty("operation_type_id") Integer operationTypeId,
    @JsonProperty("amount")            BigDecimal amount,
    @JsonProperty("event_date")        String eventDate,
    @JsonProperty("published_at")      String publishedAt
) {
}
