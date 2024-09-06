package com.example.urlshortener.url.model;

import com.example.urlshortener.model.User;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "short_urls")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "short_url", nullable = false, unique = true, length = 8)
    private String shortUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "visit_count", nullable = false)
    private int visitCount = 0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;


}
