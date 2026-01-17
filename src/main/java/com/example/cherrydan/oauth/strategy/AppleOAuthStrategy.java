package com.example.cherrydan.oauth.strategy;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.AppleOAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.AppleIdentityTokenService;
import com.example.cherrydan.oauth.strategy.OAuthStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Apple OAuth 인증 전략 구현체
 * Identity Token 기반 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthStrategy implements OAuthStrategy {
    
    private final AppleIdentityTokenService appleIdentityTokenService;
    
    @Override
    public OAuth2UserInfo getUserInfo(String token) {
        validateToken(token);
        
        try {
            // Apple Identity Token 검증 및 파싱
            Map<String, Object> claims = appleIdentityTokenService.verifyIdentityToken(token);
            
            // AppleOAuth2UserInfo가 기대하는 형식으로 변환
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", claims.get("sub"));
            attributes.put("email", claims.get("email"));
            attributes.put("email_verified", claims.get("email_verified"));
            attributes.put("is_private_email", claims.get("is_private_email"));
            
            log.info("Apple 사용자 정보 조회 성공: sub={}", claims.get("sub"));
            return new AppleOAuth2UserInfo(attributes);
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple Identity Token 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
        }
    }
    
    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }
    
    @Override
    public void validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.error("Apple Identity Token이 비어있습니다");
            throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
}