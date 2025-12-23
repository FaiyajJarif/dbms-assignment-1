package com.Eqinox.store.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Dashboard {

    @GetMapping("/dashboard")
    public String index(Model model) {
        // Later you can add stuff like:
        // model.addAttribute("name", "Faiyaj");
        return "dashboard"; // dashboard.html in templates
    }
}
