package com.example.urlshortener.url.controller;

import com.example.urlshortener.model.User;
import com.example.urlshortener.services.UserService;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;


@Controller
@RequestMapping("/url")
public class UserUrlController {

    private static final Logger logger = LoggerFactory.getLogger(UserUrlController.class);

    private final UserService userService;
    private final UrlShortenerService urlShortenerService;

    @Autowired
    public UserUrlController(UserService userService, UrlShortenerService urlShortenerService) {
        this.userService = userService;
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/update/{shortUrl}")
    public String updateUrl(@PathVariable String shortUrl, @ModelAttribute UrlRequest urlUpdateRequest, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User not authenticated, cannot update URL.");
            return "error/403";
        }

        User user = getUserFromAuthentication(authentication);

        try {
            ShortUrl shortUrlEntity = urlShortenerService.getOriginalUrl(shortUrl)
                    .orElseThrow(() -> new EntityNotFoundException("URL not found"));

            if (!shortUrlEntity.getCreatedBy().equals(user)) {
                throw new AccessDeniedException("You are not authorized to update this URL");
            }

            Duration expiryDate = convertToDuration(urlUpdateRequest.getNewExpiryDate());
            System.out.println("expiryDate = " + expiryDate);
            if (expiryDate == null) {
                expiryDate = Duration.ofMinutes(5);
            }
            LocalDateTime newExpiryDate = LocalDateTime.now().plus(expiryDate);
            shortUrlEntity.setExpiryDate(newExpiryDate);

            urlShortenerService.saveShortUrl(shortUrlEntity);

            model.addAttribute("message", "URL updated successfully");
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "You are not authorized to update this URL");
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "URL not found");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while updating the URL");
            logger.error("Error updating URL: ", e);
        }

        return "redirect:/url/my-urls";
    }

    @PostMapping("/delete/{shortUrl}")
    public String deleteUrl(
            @PathVariable String shortUrl,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User not authenticated");
            model.addAttribute("error", "You are not authorized to delete this URL");
            return "redirect:/error";
        }

        User user = getUserFromAuthentication(authentication);

        try {
            ShortUrl shortUrlEntity = urlShortenerService.getOriginalUrl(shortUrl)
                    .orElseThrow(() -> new EntityNotFoundException("URL not found"));

            if (!shortUrlEntity.getCreatedBy().equals(user)) {
                throw new AccessDeniedException("You are not authorized to delete this URL");
            }

            urlShortenerService.deleteURL(shortUrlEntity, user);
            model.addAttribute("message", "URL deleted successfully");
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "You are not authorized to delete this URL");
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "URL not found");
        }

        return "redirect:/url/my-urls";
    }

    private Duration convertToDuration(String durationStr) {
        try {

            if (durationStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                LocalDateTime parsedDateTime = LocalDateTime.parse(durationStr);

                return Duration.between(LocalDateTime.now(), parsedDateTime);
            }
            return Duration.parse(durationStr);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse duration: {}", durationStr);
            return null;
        }
    }


    @GetMapping("/error/404")
    public String show404Page() {
        return "error/404";
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.loadUserByUsername(username);
    }
}
