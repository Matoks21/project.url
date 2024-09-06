package com.example.urlshortener.security.jwt.settings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private SecretKey secretKey;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {

        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        jwtTokenProvider = new JwtTokenProvider(secretKey);
    }

    @Test
    void testCreateToken() {
        String email = "testuser@example.com";
        String token = jwtTokenProvider.createToken(email);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        Claims claims = jwtTokenProvider.getClaims(token);
        assertEquals(email, claims.getSubject());
    }

    @Test
    void testGetClaims() {
        String email = "testuser@example.com";
        String token = jwtTokenProvider.createToken(email);

        Claims claims = jwtTokenProvider.getClaims(token);
        assertEquals(email, claims.getSubject());
    }

    @Test
    void testValidateToken() {
        String email = "testuser@example.com";
        String token = jwtTokenProvider.createToken(email);

        assertTrue(jwtTokenProvider.validateToken(token));

        String invalidToken = token + "invalid";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testGetUsername() {
        String email = "testuser@example.com";
        String token = jwtTokenProvider.createToken(email);

        String username = jwtTokenProvider.getUsername(token);
        assertEquals(email, username);
    }

    @Test
    void testGetTokenFromRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwtToken", "test-token"));

        String token = jwtTokenProvider.getTokenFromRequest(request);
        assertEquals("test-token", token);

        MockHttpServletRequest emptyRequest = new MockHttpServletRequest();
        String emptyToken = jwtTokenProvider.getTokenFromRequest(emptyRequest);
        assertNull(emptyToken);
    }
}
