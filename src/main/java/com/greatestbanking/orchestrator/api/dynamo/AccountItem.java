package com.greatestbanking.orchestrator.api.dynamo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Account item in the {@code gbo-api} single-table design.
 *
 * <p>Key layout:
 * <pre>
 *   PK     = ACCOUNT#&lt;ulid&gt;
 *   SK     = #META
 *   GSI1PK = DOCNUM#&lt;kms-encrypted-base64-docnum&gt;   (uniqueness index)
 *   GSI1SK = ACCOUNT#&lt;ulid&gt;
 * </pre>
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class AccountItem {

    private String pk;
    private String sk;
    private String accountId;
    private String documentNumber; // KMS-encrypted Base64 (never plaintext on the wire)
    private String gsi1pk;
    private String gsi1sk;
    private String createdAt;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "GSI1-docnum")
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = "GSI1-docnum")
    public String getGsi1sk() { return gsi1sk; }
}
