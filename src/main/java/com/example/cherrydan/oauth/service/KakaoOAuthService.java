package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    
    private final RestTemplate restTemplate;
    
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Kakao 사용자 정보 조회 성공");
                return response.getBody();
            } else {
                log.error("Kakao 사용자 정보 조회 실패: {}", response.getStatusCode());
                throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
            }
            
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.OAUTH_AUTHENTICATION_FAILED);
        }
    }
} 