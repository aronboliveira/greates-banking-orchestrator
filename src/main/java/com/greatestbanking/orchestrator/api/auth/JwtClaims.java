package com.greatestbanking.orchestrator.api.auth;

import com.greatestbanking.orchestrator.api.entity.UserRole;

import java.time.OffsetDateTime;

public record JwtClaims(
    Long userId,
    String username,
    String displayName,
    UserRole role,
    OffsetDateTime expiresAt
) {}
