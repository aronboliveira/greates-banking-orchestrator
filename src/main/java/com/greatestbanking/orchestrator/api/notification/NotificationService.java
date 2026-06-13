package com.greatestbanking.orchestrator.api.notification;

import com.greatestbanking.orchestrator.api.dto.response.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.notifications.smtp-enabled:false}")
    private boolean smtpEnabled;

    @Value("${app.notifications.allowed-recipient:aronprogamador@gmail.com}")
    private String allowedRecipient;

    @Value("${spring.mail.username:}")
    private String configuredFromAddress;

    public void accountCreated(Long accountId) {
        sendMajorNotification(
            "Account created",
            "A new account was created in The Greatest Banking Orchestrator. account_id=" + accountId
        );
    }

    public void transactionCreated(Long transactionId, Long accountId) {
        sendMajorNotification(
            "Transaction created",
            "A new transaction was created. transaction_id=" + transactionId + ", account_id=" + accountId
        );
    }

    public void profileUpdated(UserProfileResponse profile) {
        sendMajorNotification(
            "Profile updated",
            "Profile updated for " + profile.username() + " (" + profile.email() + ")"
        );
    }

    private void sendMajorNotification(String subject, String body) {
        if (!smtpEnabled) {
            log.info("[SMTP:disabled] {} -> {} :: {}", subject, allowedRecipient, body);
            return;
        }

        if (!"aronprogamador@gmail.com".equalsIgnoreCase(allowedRecipient)) {
            log.warn("[SMTP:blocked] Refusing real email send to non-demo recipient: {}", allowedRecipient);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (configuredFromAddress != null && !configuredFromAddress.isBlank()) {
                message.setFrom(configuredFromAddress);
            }
            message.setTo(allowedRecipient);
            message.setSubject("[GBO] " + subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[SMTP:sent] {}", subject);
        } catch (RuntimeException ex) {
            log.warn("[SMTP:failed] {}: {}", subject, ex.getMessage());
        }
    }
}
