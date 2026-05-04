package com.pismochallenge.api.service;

import com.pismochallenge.api.cipher.DocumentNumberCipher;
import com.pismochallenge.api.cipher.NoOpDocumentNumberCipher;
import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.event.AccountCreatedEvent;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    /** Real NoOp cipher: pass-through encode/decode covers the production code paths. */
    @Spy
    private DocumentNumberCipher documentNumberCipher = new NoOpDocumentNumberCipher();

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldReturnAccountResponse() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(accountRepository).save(any(Account.class));
        verify(documentNumberCipher).encode("12345678900");
        verify(documentNumberCipher).decode("12345678900");
        verify(applicationEventPublisher).publishEvent(any(AccountCreatedEvent.class));
    }

    @Test
    void getAccount_shouldReturnAccountResponse_whenExists() {
        Account account = new Account(1L, "12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(documentNumberCipher).decode("12345678900");
    }

    @Test
    void getAccount_shouldThrowException_whenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Account not found");
    }
}
