package com.example.urlshortener.url.controller;

import lombok.Data;

import java.time.Duration;


@Data
public class UrlRequest {

    private String originalUrl;

    private Duration expiryDate;

    private String newExpiryDate;


}
