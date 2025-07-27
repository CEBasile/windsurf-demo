package com.ticketapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "app.security.mock-jwt", havingValue = "true")
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder mockJwtDecoder() {
        return new MockJwtDecoder();
    }

    public static class MockJwtDecoder implements JwtDecoder {
        
        @Override
        public Jwt decode(String token) throws RuntimeException {
            // Parse mock token format: "mock-{userType}-{userId}"
            // Examples: "mock-admin-user123", "mock-support-user456", "mock-user-user789"
            
            String[] parts = token.split("-");
            if (parts.length != 3 || !parts[0].equals("mock")) {
                throw new RuntimeException("Invalid mock token format. Use: mock-{userType}-{userId}");
            }
            
            String userType = parts[1];
            String userId = parts[2];
            
            List<String> roles = getRolesForUserType(userType);
            
            Map<String, Object> claims = Map.of(
                "SID", userId,
                "roles", roles,
                "sub", userId,
                "iss", "mock-issuer",
                "aud", "ticket-app",
                "exp", Instant.now().plusSeconds(3600).getEpochSecond(),
                "iat", Instant.now().getEpochSecond()
            );
            
            Map<String, Object> headers = Map.of(
                "alg", "none",
                "typ", "JWT"
            );
            
            return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        }
        
        private List<String> getRolesForUserType(String userType) {
            return switch (userType.toLowerCase()) {
                case "admin" -> List.of("ADMIN", "SUPPORT", "USER");
                case "support" -> List.of("SUPPORT", "USER");
                case "user" -> List.of("USER");
                default -> throw new RuntimeException("Unknown user type: " + userType + ". Use: admin, support, or user");
            };
        }
    }
}
