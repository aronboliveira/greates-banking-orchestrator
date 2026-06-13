package com.greatestbanking.orchestrator.api.controller;

import com.greatestbanking.orchestrator.api.auth.JwtClaims;
import com.greatestbanking.orchestrator.api.dto.request.UpdateUserProfileRequest;
import com.greatestbanking.orchestrator.api.dto.response.AvatarOptionResponse;
import com.greatestbanking.orchestrator.api.dto.response.UserProfileResponse;
import com.greatestbanking.orchestrator.api.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Profile and avatar operations for the dashboard user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/users/me/profile")
    @Operation(summary = "Get profile", description = "Returns the current user's editable dashboard profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getProfile((JwtClaims) authentication.getPrincipal()));
    }

    @PatchMapping("/users/me/profile")
    @Operation(summary = "Update profile", description = "Updates display name, email, avatar, and notification preference")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile((JwtClaims) authentication.getPrincipal(), request));
    }

    @GetMapping("/profile-avatars")
    @Operation(summary = "List avatars", description = "Returns the static avatar catalog used by the dashboard")
    public ResponseEntity<List<AvatarOptionResponse>> getAvatars() {
        return ResponseEntity.ok(userProfileService.getAvatarOptions());
    }
}
