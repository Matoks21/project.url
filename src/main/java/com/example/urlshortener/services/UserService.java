package com.example.urlshortener.services;


import com.example.urlshortener.model.ERole;
import com.example.urlshortener.model.Role;
import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.RoleRepository;
import com.example.urlshortener.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;

    }

    @Transactional
    public void registerUser(User user, BindingResult bindingResult) {
        if (userRepository.existsByEmail(user.getEmail())) {
            bindingResult.addError(new FieldError("user", "email", "Email is already in use"));
            System.out.println("Email is already in use");
            return;
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            bindingResult.addError(new FieldError("user", "username", "Username is already in use"));
            System.out.println("Username is already in use");
            return;
        }

        if (!isValidPassword(user.getPassword())) {
            bindingResult.addError(new FieldError("user", "password", "Password must be at least 8 characters long, including digits, uppercase, and lowercase letters"));
            System.out.println("Invalid password");
            return;
        }

        if (bindingResult.hasErrors()) {
            System.out.println("BindingResult has errors, stopping the registration process");
            return;
        }

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("passwordEncoder.encode(user.getPassword()) = " + passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
    }



    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit;
    }


    @Transactional
    public User loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }


    public void saveUser(User existingUser) {
        userRepository.save(existingUser);
    }
}