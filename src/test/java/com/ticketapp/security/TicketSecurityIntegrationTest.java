package com.ticketapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.model.Ticket;
import com.ticketapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "app.security.mock-jwt=false", // Disable mock JWT for proper testing
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://mock-oidc-provider.com/.well-known/jwks.json"
})
@Transactional
class TicketSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Clean database before each test
        ticketRepository.deleteAll();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should require authentication for API endpoints")
        void shouldRequireAuthenticationForApiEndpoints() throws Exception {
            mockMvc.perform(get("/api/tickets"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow access to actuator endpoints without authentication")
        void shouldAllowAccessToActuatorEndpoints() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to static resources without authentication")
        void shouldAllowAccessToStaticResources() throws Exception {
            mockMvc.perform(get("/"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Ticket Creation Tests")
    class TicketCreationTests {

        @Test
        @DisplayName("Admin should be able to create tickets")
        void adminShouldBeAbleToCreateTickets() throws Exception {
            Ticket ticket = createTestTicket("Admin Ticket", "Admin created ticket");

            mockMvc.perform(post("/api/tickets")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "admin123")
                            .claim("roles", java.util.List.of("ADMIN", "SUPPORT", "USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ticket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Admin Ticket"))
                    .andExpect(jsonPath("$.createdBy").value("admin123"));
        }

        @Test
        @DisplayName("Support should be able to create tickets")
        void supportShouldBeAbleToCreateTickets() throws Exception {
            Ticket ticket = createTestTicket("Support Ticket", "Support created ticket");

            mockMvc.perform(post("/api/tickets")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "support456")
                            .claim("roles", java.util.List.of("SUPPORT", "USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ticket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Support Ticket"))
                    .andExpect(jsonPath("$.createdBy").value("support456"));
        }

        @Test
        @DisplayName("Regular user should be able to create tickets")
        void regularUserShouldBeAbleToCreateTickets() throws Exception {
            Ticket ticket = createTestTicket("User Ticket", "User created ticket");

            mockMvc.perform(post("/api/tickets")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user789")
                            .claim("roles", java.util.List.of("USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ticket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("User Ticket"))
                    .andExpect(jsonPath("$.createdBy").value("user789"));
        }
    }

    @Nested
    @DisplayName("View All Tickets Tests")
    class ViewAllTicketsTests {

        @BeforeEach
        void createTestData() {
            // Create test tickets
            createAndSaveTicket("user123", "User Ticket 1", "Description 1");
            createAndSaveTicket("user456", "User Ticket 2", "Description 2");
            createAndSaveTicket("admin789", "Admin Ticket", "Description 3");
        }

        @Test
        @DisplayName("Admin should be able to view all tickets")
        void adminShouldBeAbleToViewAllTickets() throws Exception {
            mockMvc.perform(get("/api/tickets")
                    .with(jwt()
                            .jwt(jwt -> jwt
                                    .claim("SID", "admin123")
                                    .claim("roles", java.util.List.of("ADMIN", "SUPPORT", "USER")))
                            .authorities(java.util.List.of(
                                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                                    new SimpleGrantedAuthority("ROLE_SUPPORT"),
                                    new SimpleGrantedAuthority("ROLE_USER")))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("Support should be able to view all tickets")
        void supportShouldBeAbleToViewAllTickets() throws Exception {
            mockMvc.perform(get("/api/tickets")
                    .with(jwt()
                            .jwt(jwt -> jwt
                                    .claim("SID", "support456")
                                    .claim("roles", java.util.List.of("SUPPORT", "USER")))
                            .authorities(java.util.List.of(
                                    new SimpleGrantedAuthority("ROLE_SUPPORT"),
                                    new SimpleGrantedAuthority("ROLE_USER")))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("Regular user should NOT be able to view all tickets")
        void regularUserShouldNotBeAbleToViewAllTickets() throws Exception {
            mockMvc.perform(get("/api/tickets")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user789")
                            .claim("roles", java.util.List.of("USER")))))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("View Own Tickets Tests")
    class ViewOwnTicketsTests {

        @BeforeEach
        void createTestData() {
            createAndSaveTicket("user123", "User 123 Ticket 1", "Description 1");
            createAndSaveTicket("user123", "User 123 Ticket 2", "Description 2");
            createAndSaveTicket("user456", "User 456 Ticket", "Description 3");
        }

        @Test
        @DisplayName("User should be able to view only their own tickets")
        void userShouldBeAbleToViewOnlyTheirOwnTickets() throws Exception {
            mockMvc.perform(get("/api/tickets/my")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user123")
                            .claim("roles", java.util.List.of("USER")))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].createdBy").value("user123"))
                    .andExpect(jsonPath("$[1].createdBy").value("user123"));
        }
    }

    @Nested
    @DisplayName("View Specific Ticket Tests")
    class ViewSpecificTicketTests {

        private Long userTicketId;
        private Long otherUserTicketId;

        @BeforeEach
        void createTestData() {
            Ticket userTicket = createAndSaveTicket("user123", "User 123 Ticket", "Description 1");
            Ticket otherUserTicket = createAndSaveTicket("user456", "User 456 Ticket", "Description 2");
            
            userTicketId = userTicket.getId();
            otherUserTicketId = otherUserTicket.getId();
        }

        @Test
        @DisplayName("User should be able to view their own ticket")
        void userShouldBeAbleToViewTheirOwnTicket() throws Exception {
            mockMvc.perform(get("/api/tickets/" + userTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user123")
                            .claim("roles", java.util.List.of("USER")))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdBy").value("user123"));
        }

        @Test
        @DisplayName("User should NOT be able to view other user's ticket")
        void userShouldNotBeAbleToViewOtherUsersTicket() throws Exception {
            mockMvc.perform(get("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user123")
                            .claim("roles", java.util.List.of("USER")))))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin should be able to view any ticket")
        void adminShouldBeAbleToViewAnyTicket() throws Exception {
            mockMvc.perform(get("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "admin123")
                            .claim("roles", java.util.List.of("ADMIN", "SUPPORT", "USER")))))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Support should be able to view any ticket")
        void supportShouldBeAbleToViewAnyTicket() throws Exception {
            mockMvc.perform(get("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "support456")
                            .claim("roles", java.util.List.of("SUPPORT", "USER")))))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Update Ticket Tests")
    class UpdateTicketTests {

        private Long userTicketId;
        private Long otherUserTicketId;

        @BeforeEach
        void createTestData() {
            Ticket userTicket = createAndSaveTicket("user123", "User 123 Ticket", "Description 1");
            Ticket otherUserTicket = createAndSaveTicket("user456", "User 456 Ticket", "Description 2");
            
            userTicketId = userTicket.getId();
            otherUserTicketId = otherUserTicket.getId();
        }

        @Test
        @DisplayName("User should be able to update their own ticket")
        void userShouldBeAbleToUpdateTheirOwnTicket() throws Exception {
            Ticket updatedTicket = createTestTicket("Updated Title", "Updated Description");

            mockMvc.perform(put("/api/tickets/" + userTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user123")
                            .claim("roles", java.util.List.of("USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.createdBy").value("user123")); // Should preserve original creator
        }

        @Test
        @DisplayName("User should NOT be able to update other user's ticket")
        void userShouldNotBeAbleToUpdateOtherUsersTicket() throws Exception {
            Ticket updatedTicket = createTestTicket("Updated Title", "Updated Description");

            mockMvc.perform(put("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user123")
                            .claim("roles", java.util.List.of("USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedTicket)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin should be able to update any ticket")
        void adminShouldBeAbleToUpdateAnyTicket() throws Exception {
            Ticket updatedTicket = createTestTicket("Admin Updated", "Admin updated this ticket");

            mockMvc.perform(put("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "admin123")
                            .claim("roles", java.util.List.of("ADMIN", "SUPPORT", "USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Admin Updated"))
                    .andExpect(jsonPath("$.createdBy").value("user456")); // Should preserve original creator
        }

        @Test
        @DisplayName("Support should be able to update any ticket")
        void supportShouldBeAbleToUpdateAnyTicket() throws Exception {
            Ticket updatedTicket = createTestTicket("Support Updated", "Support updated this ticket");

            mockMvc.perform(put("/api/tickets/" + otherUserTicketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "support789")
                            .claim("roles", java.util.List.of("SUPPORT", "USER"))))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Support Updated"))
                    .andExpect(jsonPath("$.createdBy").value("user456")); // Should preserve original creator
        }
    }

    @Nested
    @DisplayName("Delete Ticket Tests")
    class DeleteTicketTests {

        private Long ticketId;

        @BeforeEach
        void createTestData() {
            Ticket ticket = createAndSaveTicket("user123", "Test Ticket", "Test Description");
            ticketId = ticket.getId();
        }

        @Test
        @DisplayName("Admin should be able to delete any ticket")
        void adminShouldBeAbleToDeleteAnyTicket() throws Exception {
            mockMvc.perform(delete("/api/tickets/" + ticketId)
                    .with(jwt()
                            .jwt(jwt -> jwt
                                    .claim("SID", "admin123")
                                    .claim("roles", java.util.List.of("ADMIN", "SUPPORT", "USER")))
                            .authorities(java.util.List.of(
                                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                                    new SimpleGrantedAuthority("ROLE_SUPPORT"),
                                    new SimpleGrantedAuthority("ROLE_USER")))))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Support should NOT be able to delete tickets")
        void supportShouldNotBeAbleToDeleteTickets() throws Exception {
            mockMvc.perform(delete("/api/tickets/" + ticketId)
                    .with(jwt()
                            .jwt(jwt -> jwt
                                    .claim("SID", "support456")
                                    .claim("roles", java.util.List.of("SUPPORT", "USER")))
                            .authorities(java.util.List.of(
                                    new SimpleGrantedAuthority("ROLE_SUPPORT"),
                                    new SimpleGrantedAuthority("ROLE_USER")))))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user should NOT be able to delete tickets")
        void regularUserShouldNotBeAbleToDeleteTickets() throws Exception {
            mockMvc.perform(delete("/api/tickets/" + ticketId)
                    .with(jwt().jwt(jwt -> jwt
                            .claim("SID", "user789")
                            .claim("roles", java.util.List.of("USER")))))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // Helper methods
    private Ticket createTestTicket(String title, String description) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority("MEDIUM");
        ticket.setStatus("OPEN");
        return ticket;
    }

    private Ticket createAndSaveTicket(String createdBy, String title, String description) {
        Ticket ticket = createTestTicket(title, description);
        ticket.setCreatedBy(createdBy);
        return ticketRepository.save(ticket);
    }
}
