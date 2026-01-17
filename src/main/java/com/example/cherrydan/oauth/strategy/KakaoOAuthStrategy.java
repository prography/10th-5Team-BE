package com.example.cherrydan.oauth.strategy;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.KakaoOAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Kakao OAuth 인증 전략 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthStrategy implements OAuthStrategy {
    
    private static final String KAKAO_USER_INFO_ENDPOINT = "https://kapi.kakao.com/v2/user/me";
    private final RestTemplate restTemplate;
    
    @Override
    public OAuth2UserInfo getUserInfo(String token) {
        validateToken(token);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_USER_INFO_ENDPOINT,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Kakao 사용자 정보 조회 성공");
                return new KakaoOAuth2UserInfo(response.getBody());
            } else {
                log.error("Kakao 사용자 정보 조회 실패: {}", response.getStatusCode());
                throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
            }
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
        }
    }
    
    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }
    
    @Override
    public void validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.error("Kakao 액세스 토큰이 비어있습니다");
            throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
}