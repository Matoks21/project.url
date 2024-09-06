package com.example.urlshortener.url.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.urlshortener.services.UserService;
import com.example.urlshortener.security.jwt.settings.JwtTokenProvider;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class UrlShortenerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlShortenerService urlShortenerService;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UrlShortenerController urlShortenerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(urlShortenerController)
                .build();
    }

    @Test
    public void testShowShortenPage() throws Exception {
        mockMvc.perform(get("/url/shorten"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/shorten"));
    }


    @Test
    public void testRedirectToOriginalUrl_ValidShortUrl() throws Exception {
        String shortUrl = "shortUrl";
        ShortUrl shortUrlEntity = new ShortUrl();
        shortUrlEntity.setOriginalUrl("http://example.com");

        when(urlShortenerService.getOriginalUrl(shortUrl)).thenReturn(Optional.of(shortUrlEntity));
        when(urlShortenerService.isUrlExpired(shortUrlEntity)).thenReturn(false);

        mockMvc.perform(get("/url/{shortUrl}", shortUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://example.com"));
    }


    @Test
    public void testRedirectToOriginalUrl() throws Exception {
        String shortUrl = "short123";
        String originalUrl = "http://example.com";

        ShortUrl url = new ShortUrl();
        url.setShortUrl(shortUrl);
        url.setOriginalUrl(originalUrl);
        url.setCreatedAt(LocalDateTime.now().plusDays(7));

        when(urlShortenerService.getOriginalUrl(shortUrl)).thenReturn(Optional.of(url));

        mockMvc.perform(get("/url/" + shortUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(originalUrl));
    }

}