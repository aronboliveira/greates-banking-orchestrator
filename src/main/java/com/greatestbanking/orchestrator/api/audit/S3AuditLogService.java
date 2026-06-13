package com.greatestbanking.orchestrator.api.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Writes immutable audit records to an S3 bucket protected by Object Lock.
 *
 * <p>Used as a backup audit path. The primary audit pipeline is:
 * <ol>
 *   <li>Application publishes a {@code TransactionEvent} to SQS, OR DynamoDB
 *       Stream emits a record on each table mutation.</li>
 *   <li>A Lambda function consumes the queue/stream and writes to S3.</li>
 * </ol>
 *
 * <p>This in-app implementation exists so the application can write directly
 * to S3 if the queue is unavailable, or for synchronous high-importance audit
 * events (e.g. authentication failures) that must not pass through SQS.
 *
 * <p>Bucket policy enforces:
 * <ul>
 *   <li>HTTPS-only access</li>
 *   <li>SSE-KMS required ({@link ServerSideEncryption#AWS_KMS})</li>
 *   <li>Deny on any DeleteObject — the only IAM action this service needs is
 *       {@code s3:PutObject}.</li>
 * </ul>
 */
@Service
@Profile("eks")
public class S3AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(S3AuditLogService.class);
    private static final DateTimeFormatter PARTITION_FMT =
        DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucket;
    private final String prefix;

    public S3AuditLogService(S3Client s3Client,
                             ObjectMapper objectMapper,
                             @Value("${aws.s3.audit-bucket}") String bucket,
                             @Value("${aws.s3.audit-prefix:transactions/}") String prefix) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucket = bucket;
        this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
    }

    /**
     * Persist an audit record. Runs asynchronously to avoid coupling the
     * request-response latency to S3 latency.
     */
    @Async
    public void persist(Object eventPayload, String idForKey) {
        String key = prefix
            + PARTITION_FMT.format(OffsetDateTime.now()) + "/"
            + idForKey + ".json";
        try {
            byte[] body = objectMapper.writeValueAsBytes(eventPayload);
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .serverSideEncryption(ServerSideEncryption.AWS_KMS)
                    .build(),
                RequestBody.fromBytes(body));
            log.debug("[AUDIT-S3] put s3://{}/{}", bucket, key);
        } catch (JsonProcessingException e) {
            log.error("[AUDIT-S3] serialization failed for key {}: {}", key, e.getMessage());
        } catch (RuntimeException e) {
            log.error("[AUDIT-S3] put failed for key {}: {}", key, e.getMessage());
            throw e;
        }
    }
}
