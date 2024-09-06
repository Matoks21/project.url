package com.example.urlshortener.url.controller;

import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.Optional;

@Controller
public class ShortUrlController {

    private final UrlShortenerService urlShortenerService;

    @Autowired
    public ShortUrlController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }


    @GetMapping("/{shortUrl}")
    public String redirectToOriginalUrl(
            @PathVariable String shortUrl,
            HttpServletResponse response
    ) throws IOException {
        Optional<ShortUrl> shortUrlEntityOpt = urlShortenerService.getOriginalUrl(shortUrl);

        if (shortUrlEntityOpt.isEmpty() || urlShortenerService.isUrlExpired(shortUrlEntityOpt.get())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        ShortUrl shortUrlEntity = shortUrlEntityOpt.get();
        String originalUrl = shortUrlEntity.getOriginalUrl();
        urlShortenerService.incrementVisitCount(shortUrlEntity);

        return "redirect:" + originalUrl;
    }
}
