package com.greatestbanking.orchestrator.api.resilience;

import com.greatestbanking.orchestrator.api.dto.request.CreateAccountRequest;
import com.greatestbanking.orchestrator.api.dto.request.CreateTransactionRequest;
import com.greatestbanking.orchestrator.api.dto.response.AccountResponse;
import com.greatestbanking.orchestrator.api.dto.response.TransactionResponse;
import com.greatestbanking.orchestrator.api.repository.AccountRepository;
import com.greatestbanking.orchestrator.api.repository.OperationTypeRepository;
import com.greatestbanking.orchestrator.api.repository.TransactionRepository;
import com.greatestbanking.orchestrator.api.entity.OperationType;
import com.greatestbanking.orchestrator.api.service.AccountService;
import com.greatestbanking.orchestrator.api.service.TransactionService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ConcurrentStressTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private OperationTypeRepository operationTypeRepository;
    @Autowired
    private CircuitBreaker dbCircuitBreaker;

    @BeforeEach
    void cleanUp() {
        dbCircuitBreaker.reset();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        operationTypeRepository.saveAll(List.of(
            new OperationType(1, "PURCHASE"),
            new OperationType(2, "INSTALLMENT PURCHASE"),
            new OperationType(3, "WITHDRAWAL"),
            new OperationType(4, "PAYMENT")
        ));
    }

    @Test
    void shouldHandleConcurrentAccountCreation() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<AccountResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String docNumber = String.format("%011d", i);
            futures.add(executor.submit(() -> {
                startGate.await();
                return accountService.createAccount(new CreateAccountRequest(docNumber));
            }));
        }

        startGate.countDown();

        List<AccountResponse> results = new ArrayList<>();
        for (Future<AccountResponse> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }

        assertThat(results).hasSize(threadCount);
        Set<Long> ids = results.stream().map(AccountResponse::accountId).collect(Collectors.toSet());
        assertThat(ids).hasSize(threadCount);

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldHandleConcurrentTransactions_onSameAccount() throws Exception {
        AccountResponse account = accountService.createAccount(new CreateAccountRequest("99999999999"));

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int opType = (i % 4) + 1;
            futures.add(executor.submit(() -> {
                try {
                    startGate.await();
                    transactionService.createTransaction(
                            new CreateTransactionRequest(account.accountId(), opType, new BigDecimal("10.00")));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
                return null;
            }));
        }

        startGate.countDown();
        for (Future<?> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isEqualTo(0);

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldHandleMixedOperations_underLoad() throws Exception {
        int writerCount = 10;
        int readerCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(writerCount + readerCount);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger writeSuccess = new AtomicInteger(0);
        AtomicInteger readSuccess = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < writerCount; i++) {
            final String docNumber = String.format("W%010d", i);
            futures.add(executor.submit(() -> {
                try {
                    startGate.await();
                    accountService.createAccount(new CreateAccountRequest(docNumber));
                    writeSuccess.incrementAndGet();
                } catch (Exception e) {
                    failures.incrementAndGet();
                }
                return null;
            }));
        }

        for (int i = 0; i < readerCount; i++) {
            final long accountId = (i % writerCount) + 1;
            futures.add(executor.submit(() -> {
                try {
                    startGate.await();
                    accountService.getAccount(accountId);
                    readSuccess.incrementAndGet();
                } catch (Exception e) {
                    if (!e.getMessage().contains("not found")) {
                        failures.incrementAndGet();
                    }
                }
                return null;
            }));
        }

        startGate.countDown();
        for (Future<?> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }

        assertThat(writeSuccess.get()).isEqualTo(writerCount);
        assertThat(failures.get()).isEqualTo(0);

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldMaintainDataIntegrity_underConcurrentTransactions() throws Exception {
        List<Long> accountIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AccountResponse account = accountService.createAccount(
                    new CreateAccountRequest(String.format("A%010d", i)));
            accountIds.add(account.accountId());
        }

        int transactionsPerAccount = 20;
        int totalTransactions = accountIds.size() * transactionsPerAccount;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (Long accountId : accountIds) {
            for (int i = 0; i < transactionsPerAccount; i++) {
                final int opType = (i % 4) + 1;
                final BigDecimal amount = new BigDecimal(String.format("%.2f", 10.0 + i));
                futures.add(executor.submit(() -> {
                    try {
                        startGate.await();
                        TransactionResponse response = transactionService.createTransaction(
                                new CreateTransactionRequest(accountId, opType, amount));
                        assertThat(response.transactionId()).isNotNull();
                        assertThat(response.accountId()).isEqualTo(accountId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        throw new RuntimeException("Transaction failed for account " + accountId, e);
                    }
                    return null;
                }));
            }
        }

        startGate.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isEqualTo(totalTransactions);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(totalTransactions);

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldHandleRapidFireRequests_withoutDegradation() throws Exception {
        AccountResponse account = accountService.createAccount(new CreateAccountRequest("88888888888"));

        int burstSize = 100;
        ExecutorService executor = Executors.newFixedThreadPool(burstSize);
        List<Future<Long>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < burstSize; i++) {
            final int opType = (i % 4) + 1;
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                transactionService.createTransaction(
                        new CreateTransactionRequest(account.accountId(), opType, new BigDecimal("1.00")));
                return System.currentTimeMillis() - start;
            }));
        }

        List<Long> latencies = new ArrayList<>();
        for (Future<Long> future : futures) {
            latencies.add(future.get(30, TimeUnit.SECONDS));
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgLatency = latencies.stream().mapToLong(l -> l).average().orElse(0);

        assertThat(totalTime).as("Total time for %d transactions should be under 30s", burstSize)
                .isLessThan(30_000);
        assertThat(avgLatency).as("Average latency should be reasonable")
                .isLessThan(5_000);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(burstSize);

        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }
}
