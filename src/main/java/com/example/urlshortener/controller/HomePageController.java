package com.example.urlshortener.controller;

import com.example.urlshortener.url.model.ShortUrl;
import com.example.urlshortener.url.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomePageController {

    private final UrlShortenerService urlShortenerService;

    @Autowired
    public HomePageController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String getHomePage(Model model) {
        List<ShortUrl> urls = urlShortenerService.getActiveUrls();
        model.addAttribute("urls", urls);
        return "home";
    }
}
