package com.vipul.microservices.shortener_service.controller;

import com.vipul.microservices.shortener_service.service.ShortenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShortenerController {

    private final ShortenerService shortenerService;

    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("originalUrl");
        String shortCode = shortenerService.shortenUrl(originalUrl);
        return ResponseEntity.ok(Map.of(
                "shortCode", shortCode,
                "shortUrl", "http://localhost:8765/api/redirect/" + shortCode
        ));
    }

    @GetMapping("/urls/{shortCode}")
    public ResponseEntity<Map<String, String>> resolve(@PathVariable String shortCode) {
        return shortenerService.resolveUrl(shortCode)
                .map(url -> ResponseEntity.ok(Map.of("originalUrl", url)))
                .orElse(ResponseEntity.notFound().build());
    }
}