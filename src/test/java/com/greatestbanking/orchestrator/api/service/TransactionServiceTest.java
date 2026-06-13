package com.greatestbanking.orchestrator.api.service;

import com.greatestbanking.orchestrator.api.dto.request.CreateTransactionRequest;
import com.greatestbanking.orchestrator.api.dto.response.TransactionResponse;
import com.greatestbanking.orchestrator.api.entity.Account;
import com.greatestbanking.orchestrator.api.entity.OperationType;
import com.greatestbanking.orchestrator.api.entity.Transaction;
import com.greatestbanking.orchestrator.api.event.TransactionCreatedEvent;
import com.greatestbanking.orchestrator.api.exception.ResourceNotFoundException;
import com.greatestbanking.orchestrator.api.repository.AccountRepository;
import com.greatestbanking.orchestrator.api.repository.OperationTypeRepository;
import com.greatestbanking.orchestrator.api.repository.TransactionRepository;
import com.greatestbanking.orchestrator.api.strategy.AmountSignResolver;
import com.greatestbanking.orchestrator.api.strategy.CreditAmountSignStrategy;
import com.greatestbanking.orchestrator.api.strategy.DebitAmountSignStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationTypeRepository operationTypeRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        AmountSignResolver resolver = new AmountSignResolver(
                List.of(new DebitAmountSignStrategy(), new CreditAmountSignStrategy()));
        transactionService = new TransactionService(
                transactionRepository, accountRepository, operationTypeRepository,
                resolver, applicationEventPublisher);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void createTransaction_shouldNegateAmount_forDebitOperations(int opTypeId) {
        Account account = new Account(1L, "12345678900");
        OperationType opType = new OperationType(opTypeId, "DEBIT_TYPE");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(opTypeId)).thenReturn(Optional.of(opType));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setTransactionId(1L);
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(1L, opTypeId, new BigDecimal("100.00"));
        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.amount()).isNegative();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        verify(applicationEventPublisher).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void createTransaction_shouldKeepPositiveAmount_forPayment() {
        Account account = new Account(1L, "12345678900");
        OperationType opType = new OperationType(4, "PAYMENT");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4)).thenReturn(Optional.of(opType));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setTransactionId(1L);
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(1L, 4, new BigDecimal("123.45"));
        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.amount()).isPositive();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("123.45"));
        verify(applicationEventPublisher).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void createTransaction_shouldThrow_whenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(99L, 1, new BigDecimal("50.00"));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Account not found");
    }

    @Test
    void createTransaction_shouldThrow_whenOperationTypeNotFound() {
        Account account = new Account(1L, "12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(99)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(1L, 99, new BigDecimal("50.00"));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Operation type not found");
    }
}
