package com.ticketapp.controller;

import com.ticketapp.model.Ticket;
import com.ticketapp.service.TicketService;
import com.ticketapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class TicketController {
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private UserService userService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        // Set the createdBy field to the current user's SID
        ticket.setCreatedBy(userService.getCurrentUserSid());
        Ticket savedTicket = ticketService.createTicket(ticket);
        return new ResponseEntity<>(savedTicket, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT')")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMyTickets() {
        String currentUserSid = userService.getCurrentUserSid();
        List<Ticket> tickets = ticketService.getTicketsByCreatedBy(currentUserSid);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        
        // Users can only view their own tickets unless they have ADMIN or SUPPORT role
        String currentUserSid = userService.getCurrentUserSid();
        if (!ticket.getCreatedBy().equals(currentUserSid) && 
            !userService.hasAnyRole("ADMIN", "SUPPORT")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticketDetails) {
        Ticket existingTicket = ticketService.getTicketById(id);
        String currentUserSid = userService.getCurrentUserSid();
        
        // Users can only update their own tickets unless they have ADMIN or SUPPORT role
        if (!existingTicket.getCreatedBy().equals(currentUserSid) && 
            !userService.hasAnyRole("ADMIN", "SUPPORT")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Preserve the original createdBy field
        ticketDetails.setCreatedBy(existingTicket.getCreatedBy());
        Ticket updatedTicket = ticketService.updateTicket(id, ticketDetails);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
