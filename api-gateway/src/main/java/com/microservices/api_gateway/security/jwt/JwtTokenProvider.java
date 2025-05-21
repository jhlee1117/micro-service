package com.microservices.api_gateway.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString; // ????? ?????? ??? ????

    @Value("${jwt.access-token-expire-time}") // application.yml?? ??????? ???????? ????
    private long accessTokenExpirationTime; // ?????? ???? ?? ???? ???? (??: ?? ??????? ????)

    private SecretKey actualSecretKey; // SecretKey ????? ?????? ??? ????

    @PostConstruct // ?? ???? ?? ?? ???? ????
    public void init() {
        this.actualSecretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // getSecretKey() ?????? ???? private???? ????? ?? ???, actualSecretKey?? ???? ???
    // public SecretKey getSecretKey() {
    // return actualSecretKey;
    // }

    public String generateAccessToken(String username, String tenantId) {
        Date now = new Date();
        // accessTokenExpirationTime?? ?? ??????? ??????? ???
        Date expiryDate = new Date(now.getTime() + (accessTokenExpirationTime * 1000)); 

        return Jwts.builder()
                .subject(username)
                .issuedAt(now) // ??? ??? ???? ??? (????)
                .expiration(expiryDate) // ????? ???? ???? ???
                .claim("tenantId", tenantId)
                .signWith(actualSecretKey) // ?????? SecretKey ???
                .compact();
    }

    // ???? ??? ??? ?? ????? ???? ????? ??? ???
    public boolean validateToken(String token) { 
        try {
            Jwts.parser()
                .verifyWith(actualSecretKey) // ?????? SecretKey ???
                .build()
                .parseSignedClaims(token);
            return true; // ????? ???
        } catch (Exception e) {
            // ????? ??????? ???? ????? ???
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(actualSecretKey) // ?????? SecretKey ???
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String username = claims.getSubject();
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }
    // public String getUsernameFromToken(String token) { ... }
    // public Claims getAllClaimsFromToken(String token) { ... }

    // public static void main(String[] args) {
    //     JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    //     String token = jwtTokenProvider.generateAccessToken("testuser", "tenant1");
    //     System.out.println("Generated Token: " + token);
    // }
}