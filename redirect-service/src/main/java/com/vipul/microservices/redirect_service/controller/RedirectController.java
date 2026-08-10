package com.vipul.microservices.redirect_service.controller;

import com.vipul.microservices.redirect_service.client.ShortenerClient;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RedirectController {

    private final ShortenerClient shortenerClient;

    public RedirectController(ShortenerClient shortenerClient) {
        this.shortenerClient = shortenerClient;
    }

    @GetMapping("/redirect/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        try {
            Map<String, String> response = shortenerClient.resolve(shortCode);
            String originalUrl = response.get("originalUrl");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } catch (FeignException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }
}