package com.microservices.auth.oauth;

import com.microservices.auth.domain.entity.OAuthProvider;
import java.util.Map;

public interface OAuthUserProfileExtractor {

  OAuthProvider provider();

  OAuthUserProfile extract(Map<String, Object> attributes);
}
