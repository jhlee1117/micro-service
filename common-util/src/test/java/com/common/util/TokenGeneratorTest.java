package com.common.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenGeneratorTest {

    @Test
    void generatesUniqueOpaqueToken() {
        String first = TokenGenerator.generateOpaqueToken();
        String second = TokenGenerator.generateOpaqueToken();

        assertTrue(first.matches("[A-Za-z0-9_-]{43}"));
        assertNotEquals(first, second);
    }

    @Test
    void hashesAndMatchesToken() {
        String token = TokenGenerator.generateOpaqueToken();
        String tokenHash = TokenGenerator.hashToken(token);

        assertTrue(TokenGenerator.matches(token, tokenHash));
        assertFalse(TokenGenerator.matches("different-token", tokenHash));
        assertFalse(TokenGenerator.matches(token, "invalid-hash"));
    }
}
