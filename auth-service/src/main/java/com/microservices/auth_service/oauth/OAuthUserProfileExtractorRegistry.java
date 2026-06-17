package com.microservices.auth_service.oauth;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.microservices.auth_service.domain.entity.OAuthProvider;

@Component
public class OAuthUserProfileExtractorRegistry {

    private final Map<OAuthProvider, OAuthUserProfileExtractor> extractors = new EnumMap<>(OAuthProvider.class);

    public OAuthUserProfileExtractorRegistry(List<OAuthUserProfileExtractor> extractors) {
        extractors.forEach(extractor -> this.extractors.put(extractor.provider(), extractor));
    }

    public OAuthUserProfile extract(OAuthProvider provider, Map<String, Object> attributes) {
        OAuthUserProfileExtractor extractor = extractors.get(provider);
        if (extractor == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
        return extractor.extract(attributes);
    }
}
