package com.example.cherrydan.oauth.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMinutes;
    private final long refreshTokenValidityInDays;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.validity-in-minutes:60}") long accessTokenValidityInMinutes,
            @Value("${jwt.refresh-token.validity-in-days:14}") long refreshTokenValidityInDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
        this.refreshTokenValidityInDays = refreshTokenValidityInDays;
    }

    // Access Token 생성 (15분)
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenValidityInMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 (14일)
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenValidityInDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaimsFromToken(token).getSubject());
    }

    // 토큰에서 이메일 추출 (Access Token만)
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("type", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Access Token인지 확인
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    // Refresh Token인지 확인
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    // 토큰에서 Claims 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
