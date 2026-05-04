package com.pismochallenge.api.service;

import com.pismochallenge.api.cipher.DocumentNumberCipher;
import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.event.AccountCreatedEvent;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    /**
     * Encrypts/decrypts the {@code document_number} PII field. The default profile
     * resolves to {@code NoOpDocumentNumberCipher} (pass-through), keeping the
     * existing test suite and Docker Compose workflow unchanged. The {@code eks}
     * profile binds {@code KmsDocumentNumberCipher} via {@code @Primary}, which
     * routes through AWS KMS.
     */
    private final DocumentNumberCipher documentNumberCipher;

    /**
     * Spring's built-in event bus. The {@code SqsTransactionEventListener}
     * (eks profile) subscribes via {@code @TransactionalEventListener} and
     * forwards the event to SQS. In tests and the default profile no listener
     * exists, so {@link AccountCreatedEvent} is silently dropped.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account");
        Account account = new Account();
        account.setDocumentNumber(documentNumberCipher.encode(request.documentNumber()));
        account = accountRepository.save(account);
        log.info("Account created with ID: {}", account.getAccountId());
        applicationEventPublisher.publishEvent(new AccountCreatedEvent(account));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        log.info("Fetching account with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getAccountId(),
            documentNumberCipher.decode(account.getDocumentNumber())
        );
    }
}
