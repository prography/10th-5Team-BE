package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.OAuthStateException;
import com.example.cherrydan.common.util.JwtSecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
public class OAuthStateService {

    private final SecretKey secretKey;
    private static final long STATE_VALIDITY_IN_MINUTES = 5;

    public OAuthStateService(@Value("${jwt.secret}") String secret) {
        this.secretKey = JwtSecretKeyProvider.createSecretKey(secret);
    }

    public String createState(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(STATE_VALIDITY_IN_MINUTES, ChronoUnit.MINUTES);

        String state = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "oauth_state")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();

        log.info("OAuth state 생성 완료: userId={}", userId);
        return state;
    }

    public Long parseState(String state) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(state)
                    .getPayload();

            Long userId = Long.parseLong(claims.getSubject());
            log.info("OAuth state 검증 성공: userId={}", userId);
            return userId;

        } catch (ExpiredJwtException e) {
            log.error("만료된 OAuth state: {}", e.getMessage());
            throw new OAuthStateException(ErrorMessage.OAUTH_STATE_EXPIRED);
        } catch (SignatureException e) {
            log.error("변조된 OAuth state 감지: {}", e.getMessage());
            throw new OAuthStateException(ErrorMessage.OAUTH_STATE_INVALID);
        } catch (Exception e) {
            log.error("OAuth state 파싱 실패: {}", e.getMessage());
            throw new OAuthStateException(ErrorMessage.OAUTH_STATE_PARSE_FAILED);
        }
    }
}
