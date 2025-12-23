package com.Eqinox.store.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Show login page
    @GetMapping("/login")
    public String loginPage(Model model) {
        return "loginPage"; // loginPage.html
    }

    // Show signup page
    @GetMapping("/signup")
    public String signupPage(Model model) {
        return "signupPage"; // signupPage.html
    }
}
