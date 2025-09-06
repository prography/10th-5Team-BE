package com.example.cherrydan.oauth.strategy;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.GoogleOAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.GoogleIdentityTokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth 인증 전략 구현체
 * ID Token 기반 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthStrategy implements OAuthStrategy {
    
    private final GoogleIdentityTokenService googleIdentityTokenService;
    
    @Override
    public OAuth2UserInfo getUserInfo(String token) {
        validateToken(token);
        
        try {
            // Google ID Token 검증 및 파싱
            GoogleIdToken.Payload payload = googleIdentityTokenService.verify(token);
            
            if (payload == null) {
                log.error("Google ID Token 검증 실패");
                throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
            }
            
            // GoogleOAuth2UserInfo가 기대하는 형식으로 변환
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", payload.getSubject());
            attributes.put("name", payload.get("name"));
            attributes.put("email", payload.getEmail());
            attributes.put("picture", payload.get("picture"));
            attributes.put("email_verified", payload.getEmailVerified());
            
            log.info("Google 사용자 정보 조회 성공: email={}", payload.getEmail());
            return new GoogleOAuth2UserInfo(attributes);
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID Token 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
        }
    }
    
    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
    
    @Override
    public void validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.error("Google ID Token이 비어있습니다");
            throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
}