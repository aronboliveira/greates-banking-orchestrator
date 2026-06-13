package com.greatestbanking.orchestrator.api.config;

import com.greatestbanking.orchestrator.api.entity.OperationType;
import com.greatestbanking.orchestrator.api.entity.AppUser;
import com.greatestbanking.orchestrator.api.entity.UserRole;
import com.greatestbanking.orchestrator.api.repository.AppUserRepository;
import com.greatestbanking.orchestrator.api.repository.OperationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final OperationTypeRepository operationTypeRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (operationTypeRepository.count() == 0) {
            log.info("Initializing operation types...");
            operationTypeRepository.saveAll(List.of(
                new OperationType(1, "PURCHASE"),
                new OperationType(2, "INSTALLMENT PURCHASE"),
                new OperationType(3, "WITHDRAWAL"),
                new OperationType(4, "PAYMENT")
            ));
            log.info("Operation types initialized successfully.");
        }

        seedUser("super-admin", "orchestrate-all", "Super Admin", "aronprogamador@gmail.com",
                UserRole.SUPER_ADMIN, "robot-operator", true);
        seedUser("admin", "approve-flow", "Admin Operator", "admin@example.test",
                UserRole.ADMIN, "gbo-logo-cutout", false);
        seedUser("user", "submit-flow", "Portfolio User", "user@example.test",
                UserRole.USER, "toon-operator", false);
    }

    private void seedUser(String username, String password, String displayName, String email,
                          UserRole role, String avatarId, boolean notificationsEnabled) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setRole(role);
        user.setAvatarId(avatarId);
        user.setNotificationsEnabled(notificationsEnabled);
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        appUserRepository.save(user);
        log.info("Seeded mock user '{}'", username);
    }
}
