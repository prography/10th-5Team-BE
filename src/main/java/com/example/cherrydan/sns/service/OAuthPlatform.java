package com.example.cherrydan.sns.service;

import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.TokenResponse;
import com.example.cherrydan.sns.dto.UserInfo;
import reactor.core.publisher.Mono;

/**
 * SNS 플랫폼별 OAuth 인증을 위한 인터페이스
 * 각 플랫폼은 이 인터페이스를 구현하여 표준화된 OAuth 플로우를 제공합니다.
 */
public interface OAuthPlatform {
    
    /**
     * OAuth 인증 URL을 생성합니다.
     * @return 인증 URL
     */
    String generateAuthUrl();
    
    /**
     * 인증 코드를 사용하여 액세스 토큰을 획득합니다.
     * @param code OAuth 인증 코드
     * @return 토큰 응답 정보
     */
    Mono<TokenResponse> getAccessToken(String code);
    
    /**
     * 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     * @param accessToken 액세스 토큰
     * @return 사용자 정보
     */
    Mono<UserInfo> getUserInfo(String accessToken);
    
    /**
     * 해당 플랫폼을 반환합니다.
     * @return SNS 플랫폼
     */
    SnsPlatform getPlatform();
    
    /**
     * 플랫폼의 표시 이름을 반환합니다.
     * @return 플랫폼 표시 이름
     */
    default String getDisplayName() {
        return getPlatform().getDisplayName();
    }
    
    /**
     * 플랫폼 코드를 반환합니다.
     * @return 플랫폼 코드
     */
    default String getPlatformCode() {
        return getPlatform().getPlatformCode();
    }
} 