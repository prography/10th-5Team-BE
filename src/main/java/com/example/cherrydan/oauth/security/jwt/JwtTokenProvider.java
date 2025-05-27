package com.example.capstone.oauth.security.jwt;

import com.example.capstone.common.annotation.LogExecutionTime;
import com.example.capstone.common.exception.BaseException;
import com.example.capstone.common.exception.ErrorMessage;
import com.example.capstone.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKeyGenerator secretKeyGenerator;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @LogExecutionTime
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String secret = secretKeyGenerator.getSecret();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        log.debug("JWT 토큰 생성 중 - 사용자: {}", userPrincipal.getUsername());
        
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("role", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .signWith(key)
                .compact();
    }

    @LogExecutionTime
    public String getUserEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKeyGenerator.getSecret().getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            throw new BaseException(ErrorMessage.AUTH_EXPIRED_TOKEN);
        } catch (Exception ex) {
            throw new BaseException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }

    @LogExecutionTime
    public boolean validateToken(String authToken) {
        try {
            String secret = secretKeyGenerator.getSecret();
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
                
            log.debug("JWT 토큰 검증 성공");
            return true;
        } catch (MalformedJwtException ex) {
            log.error("유효하지 않은 JWT 토큰: {}", ex.getMessage());
            throw new BaseException(ErrorMessage.AUTH_INVALID_TOKEN);
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰: {}", ex.getMessage());
            throw new BaseException(ErrorMessage.AUTH_EXPIRED_TOKEN);
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰: {}", ex.getMessage());
            throw new BaseException(ErrorMessage.AUTH_INVALID_TOKEN);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string이 비어있음: {}", ex.getMessage());
            throw new BaseException(ErrorMessage.AUTH_INVALID_TOKEN);
        } catch (Exception ex) {
            log.error("JWT 토큰 검증 중 예상치 못한 오류: {}", ex.getMessage());
            throw new BaseException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
}