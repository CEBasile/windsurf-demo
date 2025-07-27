package com.ticketapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    // Serve the Angular app for all non-API routes
    @RequestMapping(value = {"/", "/submit", "/tickets"})
    public String index() {
        return "forward:/index.html";
    }
}
