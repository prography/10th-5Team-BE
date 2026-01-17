package com.example.cherrydan.oauth.security.jwt;

import com.example.cherrydan.common.util.JwtSecretKeyProvider;
import com.example.cherrydan.oauth.dto.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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
        this.secretKey = JwtSecretKeyProvider.createSecretKey(secret);
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
        this.refreshTokenValidityInDays = refreshTokenValidityInDays;
    }

    /**
     * Access Token과 Refresh Token을 함께 생성하여 TokenDTO로 반환
     */
    public TokenDTO generateTokens(Long userId, String email) {
        String accessToken = generateAccessToken(userId, email);
        String refreshToken = generateRefreshToken(userId);

        log.info("토큰 생성 완료: 사용자 ID = {}", userId);

        return new TokenDTO(accessToken, refreshToken);
    }

    // Access Token 생성 (15분)
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenValidityInMinutes, ChronoUnit.MINUTES);

        return Jwts.builder().subject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 (14일)
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenValidityInDays, ChronoUnit.DAYS);

        return Jwts.builder().subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
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

    // 토큰 유효성 검증 + 예외 처리
    public void validateToken(String token) {
        try {
            getClaimsFromToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
            throw e;
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
                .build().parseSignedClaims(token).getPayload();
    }
}
