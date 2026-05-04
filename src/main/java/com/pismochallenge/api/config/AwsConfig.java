package com.pismochallenge.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS SDK v2 client beans.
 *
 * <p>Activated by Spring profile {@code eks}. Default profile (used in tests)
 * does not load these beans, so the application can run locally against
 * Docker Compose / H2 without any AWS dependency at runtime.
 *
 * <p>Authentication: the {@link DefaultCredentialsProvider} chain resolves
 * credentials in this order — environment variables, system properties,
 * <strong>EKS IRSA</strong> (projected service-account token, the production
 * mechanism), instance profile. No static credentials are placed in code.
 */
@Configuration
@Profile("eks")
@EnableAsync       // S3AuditLogService.persist() runs off the request thread
@EnableScheduling  // OutboxPublisher.publish() runs on a fixed delay (eks-outbox profile)
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Bean
    public KmsClient kmsClient() {
        return KmsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
