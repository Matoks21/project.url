package com.example.urlshortener.url.service;


import com.example.urlshortener.model.User;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.repository.ShortUrlRepository;
import com.example.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

 class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheManager = new ConcurrentMapCacheManager();
    }

    @Test
    void createShortUrl() {
        User user = new User();
        String originalUrl = "http://example.com";
        Duration activeDuration = Duration.ofDays(1);
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortUrl("shortUrl");
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setExpiryDate(shortUrl.getCreatedAt().plus(activeDuration));
        shortUrl.setCreatedBy(user);

        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortUrl createdShortUrl = urlShortenerService.createShortUrl(originalUrl, user, activeDuration);

        assertNotNull(createdShortUrl);
        assertEquals(originalUrl, createdShortUrl.getOriginalUrl());
        assertEquals("shortUrl", createdShortUrl.getShortUrl());
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    void getActiveUrls() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(shortUrlRepository.findByExpiryDateAfter(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(shortUrl));

        List<ShortUrl> activeUrls = urlShortenerService.getActiveUrls();

        assertNotNull(activeUrls);
        assertFalse(activeUrls.isEmpty());
        assertEquals(1, activeUrls.size());
        assertEquals(shortUrl, activeUrls.get(0));
    }

    @Test
    void getOriginalUrl() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortUrl("shortUrl");
        when(shortUrlRepository.findByShortUrl(anyString())).thenReturn(Optional.of(shortUrl));

        Optional<ShortUrl> foundUrl = urlShortenerService.getOriginalUrl("shortUrl");

        assertTrue(foundUrl.isPresent());
        assertEquals("shortUrl", foundUrl.get().getShortUrl());
    }

    @Test
    void getUrlsByUser() {
        User user = new User();
        ShortUrl shortUrl = new ShortUrl();
        when(shortUrlRepository.findByCreatedBy(any(User.class)))
                .thenReturn(Collections.singletonList(shortUrl));

        List<ShortUrl> userUrls = urlShortenerService.getUrlsByUser(user);

        assertNotNull(userUrls);
        assertFalse(userUrls.isEmpty());
        assertEquals(shortUrl, userUrls.get(0));
    }

    @Test
    void incrementVisitCount() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setVisitCount(5);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        urlShortenerService.incrementVisitCount(shortUrl);

        assertEquals(6, shortUrl.getVisitCount());
        verify(shortUrlRepository, times(1)).save(shortUrl);
    }

    @Test
    void deleteURL() {
        User user = new User();
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setCreatedBy(user);

        when(shortUrlRepository.existsByShortUrl(anyString())).thenReturn(true);

        urlShortenerService.deleteURL(shortUrl, user);

        verify(shortUrlRepository, times(1)).delete(shortUrl);
    }

    @Test
    void generateShortUrl() {
        String shortUrl = urlShortenerService.generateShortUrl();

        assertNotNull(shortUrl);
        assertEquals(8, shortUrl.length());
    }
}
