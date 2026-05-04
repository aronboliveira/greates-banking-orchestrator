package com.pismochallenge.api.dynamo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.math.BigDecimal;

/**
 * Transaction item in the {@code pismo-api} single-table design.
 *
 * <p>Key layout:
 * <pre>
 *   PK        = ACCOUNT#&lt;ulid&gt;
 *   SK        = TXN#&lt;ISO8601_eventDate&gt;#&lt;ulid&gt;   (lex-sorted by date)
 *   eventDate = ISO-8601 string                  (also exposed as a top-level
 *                                                  attribute for GSI2 range queries)
 * </pre>
 *
 * <p>{@code amount} is stored as a {@link BigDecimal}; the AWS Enhanced Client
 * serialises it as a DynamoDB Number (N) preserving precision.
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class TransactionItem {

    private String pk;
    private String sk;
    private String transactionId;
    private String accountId;
    private Integer operationTypeId;
    private BigDecimal amount;
    private String eventDate;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    /** GSI2 sort key — query transactions by account within a date range. */
    @DynamoDbSecondarySortKey(indexNames = "GSI2-eventdate")
    public String getEventDate() { return eventDate; }
}
