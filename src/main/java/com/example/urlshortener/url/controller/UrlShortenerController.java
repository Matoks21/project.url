package com.example.urlshortener.url.controller;

import com.example.urlshortener.model.User;
import com.example.urlshortener.services.UserService;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/url")
public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);

    private final UrlShortenerService urlShortenerService;
    private final UserService userService;

    @Autowired
    public UrlShortenerController(UrlShortenerService urlShortenerService, UserService userService) {
        this.urlShortenerService = urlShortenerService;
        this.userService = userService;
    }

    @GetMapping("/shorten")
    public String showShortenPage(Model model) {
        model.addAttribute("urlRequest", new UrlRequest());
        return "url/shorten";
    }

    @PostMapping("/shorten")
    public String shortenUrl(
            @ModelAttribute UrlRequest urlRequest,
            HttpServletRequest request,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Invalid or expired JWT token");
            return "error";
        }

        User user = getUserFromAuthentication(authentication);

        Duration expiryDate = urlRequest.getExpiryDate() != null ? urlRequest.getExpiryDate() : Duration.ofMinutes(5);
        ShortUrl shortUrl = urlShortenerService.createShortUrl(urlRequest.getOriginalUrl(), user, expiryDate);

        model.addAttribute("shortUrl", shortUrl);
        return "url/shorten";
    }

    @GetMapping("/{shortUrl}")
    public String redirectToOriginalUrl(
            @PathVariable String shortUrl,
            HttpServletResponse response
    ) throws IOException {
        Optional<ShortUrl> shortUrlEntityOpt = urlShortenerService.getOriginalUrl(shortUrl);
        System.out.println("shortUrlEntityOpt = " + shortUrlEntityOpt);
        if (shortUrlEntityOpt.isEmpty() || urlShortenerService.isUrlExpired(shortUrlEntityOpt.get())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        ShortUrl shortUrlEntity = shortUrlEntityOpt.get();
        logger.info("Incrementing visit count for URL: {}", shortUrlEntity.getShortUrl());
        urlShortenerService.incrementVisitCount(shortUrlEntity);

        String originalUrl = shortUrlEntity.getOriginalUrl();
        return "redirect:" + originalUrl;

    }

    @GetMapping("/my-urls")
    public String getUserShortenedUrlsPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User not authenticated");
            return "error/403";
        }

        User user = getUserFromAuthentication(authentication);
        logger.info("Authenticated user: {}", user.getUsername());

        List<ShortUrl> userUrls = urlShortenerService.getUrlsByUser(user);
        if (userUrls == null || userUrls.isEmpty()) {
            logger.info("No URLs found for user: {}", user.getUsername());
        } else {
            logger.info("URLs found: {}", userUrls);
        }

        model.addAttribute("urls", userUrls);
        return "url/my-urls";
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.loadUserByUsername(username);
    }
}
