package com.greatestbanking.orchestrator.api.dto.response;

import java.time.OffsetDateTime;

public record LoginResponse(
    String accessToken,
    String tokenType,
    OffsetDateTime expiresAt,
    UserProfileResponse user
) {}
