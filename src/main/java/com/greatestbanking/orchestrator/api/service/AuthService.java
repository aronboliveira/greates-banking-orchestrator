package com.greatestbanking.orchestrator.api.service;

import com.greatestbanking.orchestrator.api.auth.JwtService;
import com.greatestbanking.orchestrator.api.dto.request.LoginRequest;
import com.greatestbanking.orchestrator.api.dto.response.LoginResponse;
import com.greatestbanking.orchestrator.api.entity.AppUser;
import com.greatestbanking.orchestrator.api.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final UserProfileService userProfileService;

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> unauthorized());

        if (!user.getPassword().equals(request.password())) {
            throw unauthorized();
        }

        return new LoginResponse(
            jwtService.issueToken(user),
            "Bearer",
            jwtService.expiresAt(),
            userProfileService.toResponse(user)
        );
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
