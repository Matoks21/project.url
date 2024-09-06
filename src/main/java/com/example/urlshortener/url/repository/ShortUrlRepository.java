package com.example.urlshortener.url.repository;

import com.example.urlshortener.model.User;
import com.example.urlshortener.url.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortUrl(String shortUrl);

    List<ShortUrl> findByCreatedBy(User user);

    boolean existsByShortUrl(String shortUrl);

    List<ShortUrl> findByExpiryDateAfter(LocalDateTime now);


}
