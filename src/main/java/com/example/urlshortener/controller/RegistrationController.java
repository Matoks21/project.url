package com.example.urlshortener.controller;

import com.example.urlshortener.model.*;
import com.example.urlshortener.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user,
            BindingResult result
    ) {
        userService.registerUser(user, result);

        if (result.hasErrors()) {
            return "registration";
        }

        return "redirect:/login";
    }
}
