package com.vipul.microservices.shortener_service.service;

import com.vipul.microservices.shortener_service.model.UrlMapping;
import com.vipul.microservices.shortener_service.repository.UrlMappingRepository;
import com.vipul.microservices.shortener_service.util.Base62Encoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShortenerService {

    private final UrlMappingRepository repository;

    public ShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public String shortenUrl(String originalUrl) {
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping = repository.save(mapping); // first save -> generates id

        String shortCode = Base62Encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);
        repository.save(mapping); // second save -> persists the code

        return shortCode;
    }

    public Optional<String> resolveUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl);
    }
}