package com.greatestbanking.orchestrator.api.service;

import com.greatestbanking.orchestrator.api.auth.JwtClaims;
import com.greatestbanking.orchestrator.api.dto.request.UpdateUserProfileRequest;
import com.greatestbanking.orchestrator.api.dto.response.AvatarOptionResponse;
import com.greatestbanking.orchestrator.api.dto.response.UserProfileResponse;
import com.greatestbanking.orchestrator.api.entity.AppUser;
import com.greatestbanking.orchestrator.api.exception.ResourceNotFoundException;
import com.greatestbanking.orchestrator.api.notification.NotificationService;
import com.greatestbanking.orchestrator.api.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final List<AvatarOptionResponse> AVATARS = List.of(
        new AvatarOptionResponse("gbo-logo-light", "GBO light logo", "/assets/brand/logo-light.svg"),
        new AvatarOptionResponse("gbo-logo-cutout", "GBO logo cutout", "/assets/brand/logo-light-removebg-preview.png"),
        new AvatarOptionResponse("toon-operator", "Toon operator", "/assets/profiles/toon-operator.png"),
        new AvatarOptionResponse("robot-operator", "Robot operator", "/assets/profiles/robot-operator.png"),
        new AvatarOptionResponse("green-field", "Green field", "/assets/profiles/green-field.jpg"),
        new AvatarOptionResponse("office-assistant", "Office assistant", "/assets/profiles/office-assistant.jpg")
    );

    private static final Set<String> AVATAR_IDS = AVATARS.stream()
        .map(AvatarOptionResponse::id)
        .collect(Collectors.toUnmodifiableSet());

    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(JwtClaims claims) {
        return toResponse(findByUsername(claims.username()));
    }

    @Transactional
    public UserProfileResponse updateProfile(JwtClaims claims, UpdateUserProfileRequest request) {
        if (!AVATAR_IDS.contains(request.avatarId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown avatar_id");
        }

        AppUser user = findByUsername(claims.username());
        user.setDisplayName(request.displayName().trim());
        user.setEmail(request.email().trim());
        user.setAvatarId(request.avatarId());
        user.setNotificationsEnabled(request.notificationsEnabled());
        user.setUpdatedAt(OffsetDateTime.now());

        UserProfileResponse response = toResponse(appUserRepository.save(user));
        if (response.notificationsEnabled()) {
            notificationService.profileUpdated(response);
        }
        return response;
    }

    public List<AvatarOptionResponse> getAvatarOptions() {
        return AVATARS;
    }

    public UserProfileResponse toResponse(AppUser user) {
        return new UserProfileResponse(
            user.getUserId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getRole(),
            user.getAvatarId(),
            user.isNotificationsEnabled(),
            user.getUpdatedAt()
        );
    }

    private AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
