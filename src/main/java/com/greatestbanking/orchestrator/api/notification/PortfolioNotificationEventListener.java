package com.greatestbanking.orchestrator.api.notification;

import com.greatestbanking.orchestrator.api.entity.Transaction;
import com.greatestbanking.orchestrator.api.event.AccountCreatedEvent;
import com.greatestbanking.orchestrator.api.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortfolioNotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void onAccountCreated(AccountCreatedEvent event) {
        notificationService.accountCreated(event.account().getAccountId());
    }

    @EventListener
    public void onTransactionCreated(TransactionCreatedEvent event) {
        Transaction transaction = event.transaction();
        notificationService.transactionCreated(
            transaction.getTransactionId(),
            transaction.getAccount().getAccountId()
        );
    }
}
