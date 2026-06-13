package com.greatestbanking.orchestrator.api.dynamo;

import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Transaction persistence on DynamoDB.
 *
 * <p>Each insert is a single PutItem (no cross-table transaction needed in the
 * single-table design). The service layer is responsible for verifying the
 * referenced account exists before calling {@link #save}.
 */
@Repository
@Profile("eks-dynamo")
public class DynamoTransactionRepository {

    private final DynamoDbTable<TransactionItem> table;

    public DynamoTransactionRepository(DynamoDbTable<TransactionItem> table) {
        this.table = table;
    }

    public TransactionItem save(String accountId, Integer operationTypeId, BigDecimal signedAmount) {
        String txId = UlidCreator.getMonotonicUlid().toString();
        OffsetDateTime now = OffsetDateTime.now();
        String iso = now.toString();

        TransactionItem item = new TransactionItem();
        item.setPk("ACCOUNT#" + accountId);
        item.setSk("TXN#" + iso + "#" + txId);
        item.setTransactionId(txId);
        item.setAccountId(accountId);
        item.setOperationTypeId(operationTypeId);
        item.setAmount(signedAmount);
        item.setEventDate(iso);

        table.putItem(PutItemEnhancedRequest.builder(TransactionItem.class)
            .item(item)
            .build());
        return item;
    }
}
