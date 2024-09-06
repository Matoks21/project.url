package com.example.urlshortener.rest;


import com.example.urlshortener.model.User;
import com.example.urlshortener.services.UserService;
import com.example.urlshortener.url.controller.UrlRequest;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;


@RestController
@RequestMapping("/api/url")
public class UrlShortenerApiController {

    private final UrlShortenerService urlShortenerService;
    private final UserService userService;

    @Autowired
    public UrlShortenerApiController(UrlShortenerService urlShortenerService, UserService userService) {
        this.urlShortenerService = urlShortenerService;
        this.userService = userService;
    }


    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(
            @RequestParam Long id,  // Отримання id з параметра запиту
            @RequestBody UrlRequest urlRequest,
            HttpServletRequest request) {

        User user = userService.findUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + id + " not found.");
        }

        Duration expiryDate = urlRequest.getExpiryDate() != null ? urlRequest.getExpiryDate() : Duration.ofMinutes(5);
        ShortUrl shortUrl = urlShortenerService.createShortUrl(urlRequest.getOriginalUrl(), user, expiryDate);

        return ResponseEntity.ok(shortUrl);
    }


    @PutMapping("/update/{shortUrl}")
    @Operation(summary = "Update URL expiration date", description = "Updates the expiration date of a shortened URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShortUrl.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL or request parameters"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "URL not found")
    })
    public ResponseEntity<?> updateUrl(
            @PathVariable String shortUrl,
            @RequestBody UrlRequest urlUpdateRequest) {

        User user = urlShortenerService.getUserByShortUrl(shortUrl);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + user.getId() + " not found.");
        }
        ShortUrl shortUrlEntity = urlShortenerService.getOriginalUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        if (!shortUrlEntity.getCreatedBy().equals(user)) {
            throw new AccessDeniedException("You are not authorized to update this URL");
        }

        Duration newExpiryDuration = convertToDuration(urlUpdateRequest.getNewExpiryDate());
        if (newExpiryDuration != null) {
            LocalDateTime newExpiryDate = LocalDateTime.now().plus(newExpiryDuration);
            shortUrlEntity.setExpiryDate(newExpiryDate);
        }

        urlShortenerService.saveShortUrl(shortUrlEntity);

        return ResponseEntity.ok(shortUrlEntity);
    }


    @Operation(summary = "Show URL shortening page", description = "Returns the page where users can shorten URLs.")
    @GetMapping("/{shortUrl}")
    public ResponseEntity<ShortUrl> getShortUrlDetails(@PathVariable String shortUrl) {
        ShortUrl shortUrlEntity = urlShortenerService.getOriginalUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        return ResponseEntity.ok(shortUrlEntity);
    }

    @Operation(summary = "Delete URL", description = "Deletes a shortened URL based on the shortened URL identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "URL not found")
    })
    @DeleteMapping("/delete/{shortUrl}")
    public ResponseEntity<?> deleteUrl(
            @Parameter(description = "Shortened URL to delete") @PathVariable String shortUrl,
            HttpServletRequest request) {

        User user = urlShortenerService.getUserByShortUrl(shortUrl);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + user.getId() + " not found.");
        }

        ShortUrl shortUrlEntity = urlShortenerService.getOriginalUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        urlShortenerService.deleteURL(shortUrlEntity, user);
        return ResponseEntity.noContent().build();
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.loadUserByUsername(username);
    }

    private Duration convertToDuration(String durationStr) {
        try {
            return Duration.parse(durationStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
