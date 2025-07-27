package com.ticketapp.config;

import com.ticketapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityToggleTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void testSecurityEnabledByDefault() {
        // Security should be enabled by default
        assertTrue(securityProperties.isEnabled());
    }

    @SpringBootTest
    @TestPropertySource(properties = {"spring.security.enabled=false"})
    static class SecurityDisabledTest {

        @Autowired
        private SecurityProperties securityProperties;

        @Autowired
        private UserService userService;

        @Test
        public void testSecurityDisabled() {
            // Security should be disabled when configured
            assertFalse(securityProperties.isEnabled());
            
            // UserService should provide default values when security is disabled
            assertEquals("default-user", userService.getCurrentUserSid());
            assertTrue(userService.getCurrentUserRoles().contains("ADMIN"));
            assertTrue(userService.isAuthenticated());
        }
    }
}
