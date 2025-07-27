package com.ticketapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should return current user SID from JWT token")
    void shouldReturnCurrentUserSidFromJwtToken() {
        // Given
        Map<String, Object> claims = Map.of(
            "SID", "user123",
            "roles", List.of("USER"),
            "sub", "user123"
        );
        
        Jwt jwt = createMockJwt(claims);
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);

        // When
        String userSid = userService.getCurrentUserSid();

        // Then
        assertThat(userSid).isEqualTo("user123");
    }

    @Test
    @DisplayName("Should return null when no authentication present")
    void shouldReturnNullWhenNoAuthenticationPresent() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        String userSid = userService.getCurrentUserSid();

        // Then
        assertThat(userSid).isNull();
    }

    @Test
    @DisplayName("Should return null when authentication is not JWT")
    void shouldReturnNullWhenAuthenticationIsNotJwt() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        String userSid = userService.getCurrentUserSid();

        // Then
        assertThat(userSid).isNull();
    }

    @Test
    @DisplayName("Should return user roles from JWT token")
    void shouldReturnUserRolesFromJwtToken() {
        // Given
        Map<String, Object> claims = Map.of(
            "SID", "admin123",
            "roles", List.of("ADMIN", "SUPPORT", "USER"),
            "sub", "admin123"
        );
        
        Jwt jwt = createMockJwt(claims);
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);

        // When
        List<String> roles = userService.getCurrentUserRoles();

        // Then
        assertThat(roles).containsExactly("ADMIN", "SUPPORT", "USER");
    }

    @Test
    @DisplayName("Should return empty list when no roles in JWT")
    void shouldReturnEmptyListWhenNoRolesInJwt() {
        // Given
        Map<String, Object> claims = Map.of(
            "SID", "user123",
            "sub", "user123"
        );
        
        Jwt jwt = createMockJwt(claims);
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);

        // When
        List<String> roles = userService.getCurrentUserRoles();

        // Then
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no authentication present")
    void shouldReturnEmptyListWhenNoAuthenticationPresent() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        List<String> roles = userService.getCurrentUserRoles();

        // Then
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Should return true when user has specific role")
    void shouldReturnTrueWhenUserHasSpecificRole() {
        // Given
        Map<String, Object> claims = Map.of(
            "SID", "support123",
            "roles", List.of("SUPPORT", "USER"),
            "sub", "support123"
        );
        
        Jwt jwt = createMockJwt(claims);
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);

        // When & Then
        assertThat(userService.hasRole("SUPPORT")).isTrue();
        assertThat(userService.hasRole("USER")).isTrue();
        assertThat(userService.hasRole("ADMIN")).isFalse();
    }

    @Test
    @DisplayName("Should return true when user has any of the specified roles")
    void shouldReturnTrueWhenUserHasAnyOfTheSpecifiedRoles() {
        // Given
        Map<String, Object> claims = Map.of(
            "SID", "support123",
            "roles", List.of("SUPPORT", "USER"),
            "sub", "support123"
        );
        
        Jwt jwt = createMockJwt(claims);
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);

        // When & Then
        assertThat(userService.hasAnyRole("ADMIN", "SUPPORT")).isTrue();
        assertThat(userService.hasAnyRole("ADMIN", "MANAGER")).isFalse();
        assertThat(userService.hasAnyRole("USER")).isTrue();
    }

    @Test
    @DisplayName("Should return current user authorities")
    void shouldReturnCurrentUserAuthorities() {
        // Given
        java.util.Collection<GrantedAuthority> authorities = java.util.Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_SUPPORT"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(authorities).when(authentication).getAuthorities();

        // When
        List<String> userAuthorities = userService.getCurrentUserAuthorities();

        // Then
        assertThat(userAuthorities).containsExactly("ROLE_ADMIN", "ROLE_SUPPORT", "ROLE_USER");
    }

    @Test
    @DisplayName("Should return empty list when no authentication for authorities")
    void shouldReturnEmptyListWhenNoAuthenticationForAuthorities() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        List<String> authorities = userService.getCurrentUserAuthorities();

        // Then
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("Should return true when user is authenticated")
    void shouldReturnTrueWhenUserIsAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        boolean isAuthenticated = userService.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    @DisplayName("Should return false when user is not authenticated")
    void shouldReturnFalseWhenUserIsNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean isAuthenticated = userService.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    @DisplayName("Should return false when no authentication present")
    void shouldReturnFalseWhenNoAuthenticationPresent() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean isAuthenticated = userService.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isFalse();
    }

    private Jwt createMockJwt(Map<String, Object> claims) {
        Map<String, Object> headers = Map.of("alg", "none", "typ", "JWT");
        return new Jwt(
            "mock-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }
}
