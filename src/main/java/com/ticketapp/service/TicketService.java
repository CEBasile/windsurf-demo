package com.ticketapp.service;

import com.ticketapp.model.Ticket;
import com.ticketapp.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Cacheable(value = "tickets", key = "#id")
    public Ticket getTicketById(Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        return ticket.orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    @CacheEvict(value = "tickets", key = "#id")
    public Ticket updateTicket(Long id, Ticket ticketDetails) {
        Ticket ticket = getTicketById(id);
        ticket.setTitle(ticketDetails.getTitle());
        ticket.setDescription(ticketDetails.getDescription());
        ticket.setStatus(ticketDetails.getStatus());
        ticket.setPriority(ticketDetails.getPriority());
        return ticketRepository.save(ticket);
    }

    @CacheEvict(value = "tickets", key = "#id")
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
    
    @Cacheable(value = "userTickets", key = "#createdBy")
    public List<Ticket> getTicketsByCreatedBy(String createdBy) {
        return ticketRepository.findByCreatedBy(createdBy);
    }
}
