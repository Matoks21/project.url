package com.example.urlshortener.url.service;


import com.example.urlshortener.model.User;
import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class UrlShortenerService {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    private final ShortUrlRepository shortUrlRepository;


    @Autowired
    public UrlShortenerService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;

    }

    @Transactional
    public void saveShortUrl(ShortUrl shortUrl) {
        shortUrlRepository.save(shortUrl);
    }

    @Transactional
    @CachePut(value = "shortUrlCache", key = "#shortUrl.getShortUrl()")
    public void incrementVisitCount(ShortUrl shortUrl) {
        if (shortUrl != null) {
            shortUrl.setVisitCount(shortUrl.getVisitCount() + 1);
            shortUrlRepository.save(shortUrl);

        }

    }

    @Transactional(readOnly = true)
    public User getUserByShortUrl(String shortUrl) {
        Optional<ShortUrl> byShortUrl = shortUrlRepository.findByShortUrl(shortUrl);

        return byShortUrl.get().getCreatedBy();
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, User user, Duration activeDuration) {
        String shortUrl = generateShortUrl();
        ShortUrl shortUrlEntity = new ShortUrl();
        shortUrlEntity.setOriginalUrl(originalUrl);
        shortUrlEntity.setShortUrl(shortUrl);
        shortUrlEntity.setCreatedAt(LocalDateTime.now());
        shortUrlEntity.setExpiryDate(shortUrlEntity.getCreatedAt().plus(activeDuration));
        shortUrlEntity.setCreatedBy(user);
        logger.info("Creating new ShortUrl entity: {}", shortUrlEntity);
        return shortUrlRepository.save(shortUrlEntity);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getActiveUrls() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Fetching active URLs at {}", now);

        return shortUrlRepository.findByExpiryDateAfter(now);
    }

    @Transactional(readOnly = true)
    @CachePut(value = "shortUrlCache", key = "#shortUrl")
    public Optional<ShortUrl> getOriginalUrl(String shortUrl) {
        logger.debug("Fetching original URL for shortUrl: {}", shortUrl);
        return shortUrlRepository.findByShortUrl(shortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getUrlsByUser(User user) {
        logger.debug("Fetching URLs for user ID: {}", user.getId());
        return shortUrlRepository.findByCreatedBy(user);
    }


    public boolean isUrlExpired(ShortUrl shortUrl) {
        boolean expired = LocalDateTime.now().isAfter(shortUrl.getExpiryDate());
        logger.debug("Checking if URL is expired: {}. Expired: {}", shortUrl, expired);
        return expired;
    }


    String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String shortUrl;
        do {
            StringBuilder url = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                url.append(characters.charAt(secureRandom.nextInt(characters.length())));
            }
            shortUrl = url.toString();
        } while (shortUrlRepository.existsByShortUrl(shortUrl)); // Check uniqueness
        logger.debug("Generated unique short URL: {}", shortUrl);
        return shortUrl;
    }


    @Transactional
    @CacheEvict(value = "shortUrlCache", key = "#shortUrl.getShortUrl()")
    public void deleteURL(ShortUrl shortUrl, User user) {
        if (shortUrl.getCreatedBy().equals(user)) {
            logger.info("Deleting ShortUrl: {} by user: {}", shortUrl.getShortUrl(), user.getId());
            shortUrlRepository.delete(shortUrl);

        } else {
            logger.error("User ID: {} is not authorized to delete ShortUrl: {}", user.getId(), shortUrl.getShortUrl());
            throw new RuntimeException("User not authorized to delete this URL");
        }
    }
}

