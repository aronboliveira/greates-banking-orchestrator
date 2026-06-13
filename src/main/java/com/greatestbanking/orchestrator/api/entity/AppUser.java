package com.greatestbanking.orchestrator.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 40)
    private String username;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Column(name = "avatar_id", nullable = false, length = 80)
    private String avatarId;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
