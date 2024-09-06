package com.example.urlshortener.rest;

import com.example.urlshortener.model.User;
import com.example.urlshortener.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;


    public UserRestController(UserService userService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@ModelAttribute("user") @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors occurred");
        }

        userService.registerUser(user, result);

        try {
            String userJson = objectMapper.writeValueAsString(user);
            return ResponseEntity.ok(userJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error converting user to JSON");
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @ModelAttribute("user") @Valid User userDetails, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors occurred");
        }

        try {
            User existingUser = userService.findUserById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            existingUser.setUsername(userDetails.getUsername());
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            existingUser.setEmail(userDetails.getEmail());

            userService.saveUser(existingUser);
            String userJson = objectMapper.writeValueAsString(existingUser);
            return ResponseEntity.ok(userJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user");
        }
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String userJson = objectMapper.writeValueAsString(user);
            return ResponseEntity.ok(userJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user");
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            userService.deleteUserById(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting user");
        }
    }
}
