package com.greatestbanking.orchestrator.api.controller;

import com.greatestbanking.orchestrator.api.auth.JwtClaims;
import com.greatestbanking.orchestrator.api.dto.request.LoginRequest;
import com.greatestbanking.orchestrator.api.dto.response.LoginResponse;
import com.greatestbanking.orchestrator.api.dto.response.UserProfileResponse;
import com.greatestbanking.orchestrator.api.service.AuthService;
import com.greatestbanking.orchestrator.api.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Mock JWT authentication for the portfolio dashboard")
public class AuthController {

    private final AuthService authService;
    private final UserProfileService userProfileService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Issues a local mock JWT for a seeded portfolio user")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the profile attached to the current bearer token")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getProfile((JwtClaims) authentication.getPrincipal()));
    }
}
