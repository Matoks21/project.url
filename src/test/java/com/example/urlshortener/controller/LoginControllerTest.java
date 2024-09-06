package com.example.urlshortener.controller;

import com.example.urlshortener.security.jwt.settings.JwtTokenProvider;
import com.example.urlshortener.security.jwt.settings.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.mock.web.MockHttpServletResponse;


import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LoginControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginRequest loginRequest;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginPage() {
        String result = loginController.login();
        assertEquals("login", result);
    }

    @Test
    void testSuccessfulLogin() {
        String username = "user";
        String password = "Password123";
        String jwtToken = "jwtToken";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)))
                .thenReturn(authentication);
        when(jwtTokenProvider.createToken(username)).thenReturn(jwtToken);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Model model = mock(Model.class);

        String result = loginController.login(username, password, response, model);

        assertEquals("redirect:/url/shorten", result);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).createToken(username);
        verify(model, never()).addAttribute(eq("error"), anyString());

        Cookie[] cookies = response.getCookies();
        assertEquals(1, cookies.length);
        assertEquals("jwtToken", cookies[0].getName());
        assertEquals(jwtToken, cookies[0].getValue());
    }

    @Test
    void testFailedLogin() {
        String username = "user";
        String password = "wrongPassword";

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        Model model = mock(Model.class);

        String result = loginController.login(username, password, response, model);

        assertEquals("login", result);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createToken(anyString());
        verify(model, times(1)).addAttribute("error", "Invalid email or password");

        Cookie[] cookies = response.getCookies();
        assertEquals(0, cookies.length);
    }
}
