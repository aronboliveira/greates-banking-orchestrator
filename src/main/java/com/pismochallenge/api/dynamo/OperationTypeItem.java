package com.pismochallenge.api.dynamo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Operation type reference data in the {@code pismo-api} single-table design.
 *
 * <p>Seeded once at startup; PK / SK pattern: {@code OPTYPE#&lt;id&gt;} / {@code #META}.
 * Cache in application memory after first read to avoid repeated lookups.
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class OperationTypeItem {

    private String pk;
    private String sk;
    private Integer operationTypeId;
    private String description;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }
}
