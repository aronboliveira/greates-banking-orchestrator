package com.greatestbanking.orchestrator.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank(message = "display_name is required")
    @Size(max = 80, message = "display_name must have at most 80 characters")
    String displayName,

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 160, message = "email must have at most 160 characters")
    String email,

    @NotBlank(message = "avatar_id is required")
    @Size(max = 80, message = "avatar_id must have at most 80 characters")
    String avatarId,

    boolean notificationsEnabled
) {}
