package com.greatestbanking.orchestrator.api.cipher;

/**
 * Strategy for protecting the {@code document_number} PII field at rest.
 *
 * <p>The application calls {@link #encode(String)} before persisting and
 * {@link #decode(String)} after reading. Two implementations are wired by
 * Spring profile:
 *
 * <ul>
 *   <li>{@link NoOpDocumentNumberCipher} — default; pass-through for tests
 *       and local development against Docker Compose.</li>
 *   <li>{@code KmsDocumentNumberCipher} — active in the {@code eks} profile;
 *       wraps {@link com.greatestbanking.orchestrator.api.security.KmsFieldEncryptor} to
 *       encrypt with a customer-managed KMS key.</li>
 * </ul>
 */
public interface DocumentNumberCipher {

    /** Encode the plaintext document number for storage. */
    String encode(String plaintext);

    /** Decode the stored value back to plaintext for response payloads. */
    String decode(String encoded);
}
