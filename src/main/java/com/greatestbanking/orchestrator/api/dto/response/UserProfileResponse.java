package com.greatestbanking.orchestrator.api.dto.response;

import com.greatestbanking.orchestrator.api.entity.UserRole;

import java.time.OffsetDateTime;

public record UserProfileResponse(
    Long userId,
    String username,
    String displayName,
    String email,
    UserRole role,
    String avatarId,
    boolean notificationsEnabled,
    OffsetDateTime updatedAt
) {}
