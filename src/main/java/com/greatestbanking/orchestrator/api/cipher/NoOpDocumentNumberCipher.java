package com.greatestbanking.orchestrator.api.cipher;

import org.springframework.stereotype.Component;

/**
 * Pass-through cipher used when no AWS profile is active.
 *
 * <p>Lives under the default Spring profile. The KMS-backed implementation in
 * the {@code eks} profile takes precedence via {@code @Primary}.
 */
@Component
public class NoOpDocumentNumberCipher implements DocumentNumberCipher {

    @Override
    public String encode(String plaintext) {
        return plaintext;
    }

    @Override
    public String decode(String encoded) {
        return encoded;
    }
}
