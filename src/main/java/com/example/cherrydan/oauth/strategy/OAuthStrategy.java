package com.example.cherrydan.oauth.strategy;

import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;

/**
 * OAuth 제공자별 인증 전략 인터페이스
 * Strategy Pattern을 통해 각 OAuth 제공자의 특성을 캡슐화
 */
public interface OAuthStrategy {
    
    /**
     * 액세스 토큰을 사용하여 사용자 정보 조회
     * @param token OAuth 제공자의 액세스 토큰 또는 ID 토큰
     * @return OAuth2UserInfo 사용자 정보
     */
    OAuth2UserInfo getUserInfo(String token);
    
    /**
     * OAuth 제공자 타입 반환
     * @return AuthProvider 제공자 타입
     */
    AuthProvider getProvider();
    
    /**
     * 토큰 유효성 검증
     * @param token 검증할 토큰
     * @throws com.example.cherrydan.common.exception.AuthException 토큰이 유효하지 않은 경우
     */
    void validateToken(String token);
    
    /**
     * 전략이 지원하는 제공자인지 확인
     * @param provider 확인할 제공자
     * @return 지원 여부
     */
    default boolean supports(AuthProvider provider) {
        return getProvider() == provider;
    }
}