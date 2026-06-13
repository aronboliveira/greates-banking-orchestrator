package com.greatestbanking.orchestrator.api.config;

import com.greatestbanking.orchestrator.api.dynamo.AccountItem;
import com.greatestbanking.orchestrator.api.dynamo.OperationTypeItem;
import com.greatestbanking.orchestrator.api.dynamo.TransactionItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DynamoDB clients and table references — alternative persistence backend.
 *
 * <p>Activated by profile combination {@code eks,eks-dynamo}. When this profile
 * is active, the {@code application-eks-dynamo.properties} file disables JPA,
 * Flyway, and DataSource auto-configuration so the application runs with
 * DynamoDB only.
 *
 * <p>Single-table design: all three entity kinds share the table named
 * {@code gbo-api}. See handoff.md §17.2 for the key schema.
 */
@Configuration
@Profile("eks-dynamo")
public class AwsDynamoConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.dynamodb.table-name:gbo-api}")
    private String tableName;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(client)
            .build();
    }

    @Bean
    public DynamoDbTable<AccountItem> accountTable(DynamoDbEnhancedClient enhanced) {
        return enhanced.table(tableName, TableSchema.fromBean(AccountItem.class));
    }

    @Bean
    public DynamoDbTable<TransactionItem> transactionTable(DynamoDbEnhancedClient enhanced) {
        return enhanced.table(tableName, TableSchema.fromBean(TransactionItem.class));
    }

    @Bean
    public DynamoDbTable<OperationTypeItem> operationTypeTable(DynamoDbEnhancedClient enhanced) {
        return enhanced.table(tableName, TableSchema.fromBean(OperationTypeItem.class));
    }
}
