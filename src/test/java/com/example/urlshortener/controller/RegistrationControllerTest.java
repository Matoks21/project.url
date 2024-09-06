package com.example.urlshortener.controller;


import com.example.urlshortener.model.User;
import com.example.urlshortener.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RegistrationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RegistrationController registrationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShowRegistrationForm() {
        String viewName = registrationController.showRegistrationForm(model);
        assertEquals("registration", viewName);
        verify(model, times(1)).addAttribute(eq("user"), any(User.class));
    }

    @Test
    void testRegisterUser_SuccessfulRegistration() {
        User user = new User();
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = registrationController.registerUser(user, bindingResult);
        assertEquals("redirect:/login", viewName);
        verify(userService, times(1)).registerUser(eq(user), eq(bindingResult));
    }

    @Test
    void testRegisterUser_RegistrationFailsDueToValidationErrors() {
        User user = new User();
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = registrationController.registerUser(user, bindingResult);
        assertEquals("registration", viewName);
        verify(userService, times(1)).registerUser(eq(user), eq(bindingResult));
        verify(bindingResult, times(1)).hasErrors();
    }
}