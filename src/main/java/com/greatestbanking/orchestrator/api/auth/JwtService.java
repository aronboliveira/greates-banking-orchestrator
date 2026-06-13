package com.greatestbanking.orchestrator.api.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatestbanking.orchestrator.api.entity.AppUser;
import com.greatestbanking.orchestrator.api.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String secret;
    private final String issuer;
    private final long ttlMinutes;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.objectMapper = objectMapper;
        this.clock = Clock.systemUTC();
        this.secret = secret;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueToken(AppUser user) {
        try {
            OffsetDateTime now = OffsetDateTime.now(clock);
            OffsetDateTime expiresAt = now.plusMinutes(ttlMinutes);

            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("iss", issuer);
            payload.put("sub", user.getUsername());
            payload.put("user_id", user.getUserId());
            payload.put("display_name", user.getDisplayName());
            payload.put("role", user.getRole().name());
            payload.put("iat", now.toEpochSecond());
            payload.put("exp", expiresAt.toEpochSecond());

            String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to issue JWT", ex);
        }
    }

    public JwtClaims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token must have three segments");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            if (!issuer.equals(payload.get("iss"))) {
                throw new IllegalArgumentException("Invalid token issuer");
            }

            long exp = numberClaim(payload, "exp").longValue();
            OffsetDateTime expiresAt = OffsetDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(exp), clock.getZone());
            if (!expiresAt.isAfter(OffsetDateTime.now(clock))) {
                throw new IllegalArgumentException("Token expired");
            }

            return new JwtClaims(
                numberClaim(payload, "user_id").longValue(),
                String.valueOf(payload.get("sub")),
                String.valueOf(payload.get("display_name")),
                UserRole.valueOf(String.valueOf(payload.get("role"))),
                expiresAt
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid bearer token", ex);
        }
    }

    public OffsetDateTime expiresAt() {
        return OffsetDateTime.now(clock).plusMinutes(ttlMinutes);
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private Number numberClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number;
        }
        throw new IllegalArgumentException("Missing numeric claim: " + key);
    }
}
