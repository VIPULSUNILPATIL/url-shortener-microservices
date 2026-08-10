package com.vipul.microservices.redirect_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "shortener-service")
public interface ShortenerClient {

    @GetMapping("/api/urls/{shortCode}")
    Map<String, String> resolve(@PathVariable("shortCode") String shortCode);
}