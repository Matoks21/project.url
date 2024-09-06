package com.example.urlshortener.controller;


import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HomePageControllerTest {

    @Mock
    private UrlShortenerService urlShortenerService;

    @InjectMocks
    private HomePageController homePageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRedirectToHome() {
        String result = homePageController.redirectToHome();
        assertEquals("redirect:/home", result);
    }

    @Test
    void testGetHomePage() {
        Model model = new BindingAwareModelMap();
        List<ShortUrl> urls = Arrays.asList(new ShortUrl(), new ShortUrl());

        when(urlShortenerService.getActiveUrls()).thenReturn(urls);

        String result = homePageController.getHomePage(model);

        assertEquals("home", result);
        assertEquals(urls, model.getAttribute("urls"));
        verify(urlShortenerService, times(1)).getActiveUrls();
    }
}
