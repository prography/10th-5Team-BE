package com.example.cherrydan.oauth.strategy;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.NaverOAuth2UserInfo;
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
 * Naver OAuth 인증 전략 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOAuthStrategy implements OAuthStrategy {
    
    private static final String NAVER_USER_INFO_ENDPOINT = "https://openapi.naver.com/v1/nid/me";
    private final RestTemplate restTemplate;
    
    @Override
    public OAuth2UserInfo getUserInfo(String token) {
        validateToken(token);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                NAVER_USER_INFO_ENDPOINT,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String resultCode = (String) responseBody.get("resultcode");
                
                if (!"00".equals(resultCode)) {
                    log.error("Naver API 오류: resultCode={}, message={}", 
                        resultCode, responseBody.get("message"));
                    throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
                }
                
                log.info("Naver 사용자 정보 조회 성공");
                return new NaverOAuth2UserInfo(responseBody);
            } else {
                log.error("Naver 사용자 정보 조회 실패: {}", response.getStatusCode());
                throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
            }
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Naver 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
        }
    }
    
    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }
    
    @Override
    public void validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.error("Naver 액세스 토큰이 비어있습니다");
            throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
}