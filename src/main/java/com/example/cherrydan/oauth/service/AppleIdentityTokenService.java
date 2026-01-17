package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AppleIdentityTokenService {

    @Value("${apple.keys-url}")
    private String APPLE_KEYS_URL;
    @Value("${apple.issuer}")
    private String APPLE_ISSUER;
    @Value("${apple.client-id}")
    private String APPLE_AUDIENCE;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Apple Identity Token을 검증하고 사용자 정보를 반환
     */
    public Map<String, Object> verifyIdentityToken(String identityToken) {
        try {
            log.info("Apple Identity Token 검증 시작");
            
            // 1. Apple 공개 키 가져오기
            PublicKey publicKey = getApplePublicKey(identityToken);
            
            // 2. JWT 토큰 검증
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(APPLE_AUDIENCE)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            log.info("Apple Identity Token 검증 성공: sub={}", claims.getSubject());
            
            // 3. Claims를 Map으로 변환하여 반환
            return claims;
            
        } catch (ExpiredJwtException e) {
            log.error("Apple Identity Token 만료: {}", e.getMessage());
            throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.error("Apple Identity Token 검증 실패: {}", e.getMessage());
            throw new AuthException(ErrorMessage.APPLE_JWT_VERIFICATION_FAILED);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple Identity Token 검증 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }

    /**
     * Apple 공개키를 가져와서 JWT 검증에 사용할 PublicKey 객체 생성
     */
    private PublicKey getApplePublicKey(String identityToken) {
        try {
            // JWT 헤더에서 kid 추출
            String kid = getKidFromToken(identityToken);
            log.debug("JWT에서 추출한 kid: {}", kid);
            
            // Apple 공개키 목록 가져오기
            String response = restTemplate.getForObject(APPLE_KEYS_URL, String.class);
            Map<String, Object> keys = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> keyList = (List<Map<String, Object>>) keys.get("keys");
            
            // kid와 일치하는 키 찾기
            Map<String, Object> matchingKey = keyList.stream()
                    .filter(key -> kid.equals(key.get("kid")))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("일치하는 Apple 공개키를 찾을 수 없음. kid: {}", kid);
                        return new AuthException(ErrorMessage.APPLE_PUBLIC_KEY_NOT_FOUND);
                    });
            
            // RSA 공개키 생성
            String nStr = (String) matchingKey.get("n");
            String eStr = (String) matchingKey.get("e");
            
            byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
            byte[] eBytes = Base64.getUrlDecoder().decode(eStr);
            
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);
            
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            log.debug("Apple RSA 공개키 생성 완료");
            return keyFactory.generatePublic(publicKeySpec);
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 공개키 생성 중 오류: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.APPLE_PUBLIC_KEY_NOT_FOUND);
        }
    }

    /**
     * JWT 토큰 헤더에서 kid(Key ID) 추출
     */
    private String getKidFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_INVALID);
            }
            
            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, Object> headerMap = objectMapper.readValue(header, Map.class);
            
            String kid = (String) headerMap.get("kid");
            if (kid == null || kid.trim().isEmpty()) {
                throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_INVALID);
            }
            
            return kid;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("JWT 헤더에서 kid 추출 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }
}
