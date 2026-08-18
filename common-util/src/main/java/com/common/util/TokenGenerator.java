package com.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenGenerator {

  private static final int TOKEN_BYTE_LENGTH = 32;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Encoder HASH_ENCODER = Base64.getEncoder();

  private TokenGenerator() {}

  public static String generateOpaqueToken() {
    byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
    SECURE_RANDOM.nextBytes(randomBytes);
    return URL_ENCODER.encodeToString(randomBytes);
  }

  public static String hashToken(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Token must not be blank");
    }

    return HASH_ENCODER.encodeToString(sha256(token));
  }

  public static boolean matches(String rawToken, String tokenHash) {
    if (rawToken == null || rawToken.isBlank() || tokenHash == null || tokenHash.isBlank()) {
      return false;
    }

    byte[] actualHash = sha256(rawToken);
    byte[] expectedHash;

    try {
      expectedHash = Base64.getDecoder().decode(tokenHash);
    } catch (IllegalArgumentException exception) {
      return false;
    }

    return MessageDigest.isEqual(actualHash, expectedHash);
  }

  private static byte[] sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
