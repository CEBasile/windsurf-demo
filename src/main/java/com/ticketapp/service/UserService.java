package com.ticketapp.service;

import com.ticketapp.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private SecurityProperties securityProperties;

    /**
     * Get the current user's SID from the JWT token
     * When security is disabled, returns a default user ID
     */
    public String getCurrentUserSid() {
        // If security is disabled, return a default user ID
        if (!securityProperties.isEnabled()) {
            return "default-user";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("SID");
        }
        
        return null;
    }

    /**
     * Get the current user's roles from the JWT token
     * When security is disabled, returns ADMIN role for full access
     */
    public List<String> getCurrentUserRoles() {
        // If security is disabled, return ADMIN role for full access
        if (!securityProperties.isEnabled()) {
            return List.of("ADMIN");
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object rolesClaim = jwt.getClaim("roles");
            
            if (rolesClaim instanceof List<?> roles) {
                return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            }
        }
        
        return List.of();
    }

    /**
     * Check if the current user has a specific role
     */
    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current user's authorities (roles with ROLE_ prefix)
     */
    public List<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        }
        
        return List.of();
    }

    /**
     * Check if the current user is authenticated
     * When security is disabled, always returns true
     */
    public boolean isAuthenticated() {
        // If security is disabled, consider user always authenticated
        if (!securityProperties.isEnabled()) {
            return true;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
