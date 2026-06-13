package com.greatestbanking.orchestrator.api.dynamo;

import com.github.f4b6a3.ulid.UlidCreator;
import com.greatestbanking.orchestrator.api.cipher.DocumentNumberCipher;
import com.greatestbanking.orchestrator.api.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Account persistence on DynamoDB. Replaces {@code AccountRepository} when the
 * {@code eks-dynamo} profile is active.
 *
 * <p>Uniqueness of {@code documentNumber} is enforced by a conditional write
 * on {@code GSI1PK = DOCNUM#&lt;encrypted&gt;}: if a row with that GSI key
 * already exists, {@code attribute_not_exists(GSI1PK)} fails and DynamoDB
 * raises {@link ConditionalCheckFailedException}, which the service layer
 * translates into a 422 Unprocessable Entity.
 */
@Repository
@Profile("eks-dynamo")
public class DynamoAccountRepository {

    private final DynamoDbTable<AccountItem> table;
    private final DocumentNumberCipher cipher;

    public DynamoAccountRepository(DynamoDbTable<AccountItem> table,
                                   DocumentNumberCipher cipher) {
        this.table = table;
        this.cipher = cipher;
    }

    public AccountItem save(String rawDocumentNumber) {
        String accountId = UlidCreator.getMonotonicUlid().toString();
        String encrypted = cipher.encode(rawDocumentNumber);

        AccountItem item = new AccountItem();
        item.setPk("ACCOUNT#" + accountId);
        item.setSk("#META");
        item.setAccountId(accountId);
        item.setDocumentNumber(encrypted);
        item.setGsi1pk("DOCNUM#" + encrypted);
        item.setGsi1sk("ACCOUNT#" + accountId);
        item.setCreatedAt(OffsetDateTime.now().toString());

        // Reject duplicate primary key AND duplicate document number in one call.
        Expression cond = Expression.builder()
            .expression("attribute_not_exists(PK) AND attribute_not_exists(GSI1PK)")
            .build();

        table.putItem(PutItemEnhancedRequest.builder(AccountItem.class)
            .item(item)
            .conditionExpression(cond)
            .build());
        return item;
    }

    public Optional<AccountItem> findById(String accountId) {
        Key key = Key.builder()
            .partitionValue("ACCOUNT#" + accountId)
            .sortValue("#META")
            .build();
        return Optional.ofNullable(table.getItem(key));
    }

    public AccountItem getOrThrow(String accountId) {
        return findById(accountId).orElseThrow(() ->
            new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    public boolean existsByEncryptedDocumentNumber(String encrypted) {
        QueryConditional q = QueryConditional.keyEqualTo(Key.builder()
            .partitionValue("DOCNUM#" + encrypted)
            .build());

        return table.index("GSI1-docnum")
            .query(QueryEnhancedRequest.builder()
                .queryConditional(q)
                .limit(1)
                .build())
            .stream()
            .findFirst()
            .map(page -> !page.items().isEmpty())
            .orElse(false);
    }

    /** Helper for {@link DynamoTransactionRepository} ConditionCheck operations. */
    public Map<String, AttributeValue> accountKey(String accountId) {
        return Map.of(
            "PK", AttributeValue.builder().s("ACCOUNT#" + accountId).build(),
            "SK", AttributeValue.builder().s("#META").build()
        );
    }
}
