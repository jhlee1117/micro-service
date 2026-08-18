package com.microservices.auth.oauth;

import com.microservices.auth.domain.entity.OAuthProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NaverOAuthUserProfileExtractor implements OAuthUserProfileExtractor {

  @Override
  public OAuthProvider provider() {
    return OAuthProvider.NAVER;
  }

  @Override
  @SuppressWarnings("unchecked")
  public OAuthUserProfile extract(Map<String, Object> attributes) {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    if (response == null) {
      throw new IllegalArgumentException("Naver OAuth response is missing");
    }

    return new OAuthUserProfile(
        provider(),
        stringValue(response.get("id")),
        stringValue(response.get("email")),
        stringValue(response.get("name")),
        stringValue(response.get("profile_image")));
  }

  private String stringValue(Object value) {
    return value != null ? value.toString() : null;
  }
}
