package com.microservices.auth.oauth;

import com.microservices.auth.domain.entity.OAuthProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GithubOAuthUserProfileExtractor implements OAuthUserProfileExtractor {

  @Override
  public OAuthProvider provider() {
    return OAuthProvider.GITHUB;
  }

  @Override
  public OAuthUserProfile extract(Map<String, Object> attributes) {
    return new OAuthUserProfile(
        provider(),
        stringValue(attributes.get("id")),
        stringValue(attributes.get("email")),
        stringValue(attributes.get("name")),
        stringValue(attributes.get("avatar_url")));
  }

  private String stringValue(Object value) {
    return value != null ? value.toString() : null;
  }
}
