package com.ticketapp.service;

import com.ticketapp.model.Ticket;
import com.ticketapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("TicketService Cache Tests")
class TicketServiceCacheTest {

    @Autowired
    private TicketService ticketService;

    @MockBean
    private TicketRepository ticketRepository;

    @Autowired
    private CacheManager cacheManager;

    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        // Create a test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("This is a test ticket for caching");
        testTicket.setPriority("Medium");
        testTicket.setStatus("Open");
        testTicket.setCreatedBy("testuser");
    }

    @Test
    @DisplayName("Should cache ticket on first retrieval and use cache on subsequent calls")
    void shouldCacheTicketOnFirstRetrievalAndUseCacheOnSubsequentCalls() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When - First call should hit the database
        Ticket firstCall = ticketService.getTicketById(1L);
        
        // When - Second call should use cache
        Ticket secondCall = ticketService.getTicketById(1L);
        
        // When - Third call should also use cache
        Ticket thirdCall = ticketService.getTicketById(1L);

        // Then
        assertThat(firstCall).isEqualTo(testTicket);
        assertThat(secondCall).isEqualTo(testTicket);
        assertThat(thirdCall).isEqualTo(testTicket);
        
        // Verify repository was called only once (first call)
        verify(ticketRepository, times(1)).findById(1L);
        
        // Verify cache contains the ticket
        var ticketsCache = cacheManager.getCache("tickets");
        assertThat(ticketsCache).isNotNull();
        assertThat(ticketsCache.get(1L)).isNotNull();
    }

    @Test
    @DisplayName("Should evict cache when ticket is updated")
    void shouldEvictCacheWhenTicketIsUpdated() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When - Cache the ticket first
        ticketService.getTicketById(1L);
        
        // Verify ticket is cached
        var ticketsCache = cacheManager.getCache("tickets");
        assertThat(ticketsCache).isNotNull();
        assertThat(ticketsCache.get(1L)).isNotNull();
        
        // When - Update the ticket (should evict cache)
        Ticket updatedTicket = new Ticket();
        updatedTicket.setTitle("Updated Title");
        updatedTicket.setDescription("Updated Description");
        updatedTicket.setPriority("High");
        updatedTicket.setStatus("In Progress");
        
        ticketService.updateTicket(1L, updatedTicket);

        // Then - Cache should be evicted
        assertThat(ticketsCache.get(1L)).isNull();
    }

    @Test
    @DisplayName("Should evict cache when ticket is deleted")
    void shouldEvictCacheWhenTicketIsDeleted() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When - Cache the ticket first
        ticketService.getTicketById(1L);
        
        // Verify ticket is cached
        var ticketsCache = cacheManager.getCache("tickets");
        assertThat(ticketsCache).isNotNull();
        assertThat(ticketsCache.get(1L)).isNotNull();
        
        // When - Delete the ticket (should evict cache)
        ticketService.deleteTicket(1L);

        // Then - Cache should be evicted
        assertThat(ticketsCache.get(1L)).isNull();
        
        // Verify delete was called
        verify(ticketRepository, times(1)).deleteById(1L);
    }
}
