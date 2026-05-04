package com.pismochallenge.api.cipher;

import com.pismochallenge.api.security.KmsFieldEncryptor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Production document-number cipher backed by AWS KMS.
 *
 * <p>Marked {@code @Primary} so Spring chooses this bean over
 * {@link NoOpDocumentNumberCipher} when the {@code eks} profile is active.
 *
 * <p>Note: {@link KmsFieldEncryptor} performs symmetric encryption; the same
 * plaintext yields the same ciphertext under a given key version. This is
 * required so DynamoDB's GSI1 on {@code DOCNUM#<encrypted>} continues to
 * function as a uniqueness index. Do not introduce randomized IVs here
 * without redesigning the GSI.
 */
@Component
@Profile("eks")
@Primary
public class KmsDocumentNumberCipher implements DocumentNumberCipher {

    private final KmsFieldEncryptor encryptor;

    public KmsDocumentNumberCipher(KmsFieldEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public String encode(String plaintext) {
        return encryptor.encrypt(plaintext);
    }

    @Override
    public String decode(String encoded) {
        return encryptor.decrypt(encoded);
    }
}
