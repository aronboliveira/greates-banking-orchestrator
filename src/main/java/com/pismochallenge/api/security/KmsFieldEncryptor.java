package com.pismochallenge.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Field-level encryption for PII attributes using a Customer-Managed Key (CMK).
 *
 * <p>The key ARN is read from {@code aws.kms.field-key-arn} which the EKS
 * ConfigMap populates at deploy time. The value to set in the ConfigMap is:
 * <pre>
 *   arn:aws:kms:&lt;region&gt;:&lt;account&gt;:alias/pismo-field-encryption-key
 * </pre>
 *
 * <p><strong>Operator action required:</strong> create the CMK in KMS console
 * (handoff.md §5) and set its ARN in {@code k8s/04-configmap.yaml} under
 * {@code AWS_KMS_FIELD_KEY_ARN}.
 *
 * <p>Throughput note: every {@code encrypt()} / {@code decrypt()} call invokes
 * KMS. For this API the call frequency is one per account create / read and
 * stays well within the default KMS request quota (5,500 RPS in most regions).
 * If hot accounts emerge, consider switching to envelope encryption with a
 * cached data key.
 */
@Component
@Profile("eks")
public class KmsFieldEncryptor {

    private final KmsClient kmsClient;
    private final String keyArn;

    public KmsFieldEncryptor(KmsClient kmsClient,
                             @Value("${aws.kms.field-key-arn}") String keyArn) {
        this.kmsClient = kmsClient;
        this.keyArn = keyArn;
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        SdkBytes input = SdkBytes.fromString(plaintext, StandardCharsets.UTF_8);
        EncryptResponse response = kmsClient.encrypt(r -> r
            .keyId(keyArn)
            .plaintext(input)
            .encryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT));
        return Base64.getEncoder().encodeToString(response.ciphertextBlob().asByteArray());
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        SdkBytes input = SdkBytes.fromByteArray(Base64.getDecoder().decode(ciphertext));
        DecryptResponse response = kmsClient.decrypt(r -> r
            .ciphertextBlob(input)
            .keyId(keyArn)
            .encryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT));
        return response.plaintext().asString(StandardCharsets.UTF_8);
    }
}
