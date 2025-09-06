package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.dto.LoginRequest;
import com.example.cherrydan.oauth.dto.LoginResponse;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.strategy.OAuthStrategy;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth 인증 Facade 서비스
 * 모든 OAuth 제공자의 로그인 처리를 통합 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthFacade {
    
    private final List<OAuthStrategy> strategies;
    private final OAuthUserProcessingService oAuthDomainService;
    private final UserLoginHistoryService loginHistoryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    
    private final Map<AuthProvider, OAuthStrategy> strategyMap = new HashMap<>();
    
    @PostConstruct
    public void initStrategies() {
        strategies.forEach(strategy -> 
            strategyMap.put(strategy.getProvider(), strategy)
        );
        log.info("OAuth strategies initialized: {}", strategyMap.keySet());
    }
    
    /**
     * 통합 OAuth 로그인 처리
     * @param provider OAuth 제공자
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processOAuthLogin(AuthProvider provider, LoginRequest loginRequest) {
        log.info("Processing OAuth login for provider: {}", provider);
        
        // 1. 적절한 전략 선택
        OAuthStrategy strategy = getStrategy(provider);
        
        // 2. 사용자 정보 조회
        OAuth2UserInfo userInfo = strategy.getUserInfo(loginRequest.getAccessToken());
        
        // 3. 도메인 서비스를 통한 사용자 처리
        User user = oAuthDomainService.processOAuthUser(userInfo, provider, loginRequest);
        
        // 4. 로그인 기록 저장
        loginHistoryService.recordLogin(user.getId());
        
        // 5. JWT 토큰 생성
        TokenDTO tokenDTO = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());
        
        // 6. Refresh Token 저장
        refreshTokenService.saveOrUpdateRefreshToken(user, tokenDTO.getRefreshToken());
        
        log.info("{} OAuth login successful: userId={}, email={}", 
            provider, user.getId(), user.getEmail());
        
        return new LoginResponse(tokenDTO, user.getId());
    }
    
    
    /**
     * OAuth2 flow를 통한 로그인 처리 (웹 기반)
     * Spring Security OAuth2와 통합
     */
    @Transactional
    public User processOAuth2User(OAuth2UserInfo userInfo, AuthProvider provider) {
        log.info("Processing OAuth2 user for provider: {}", provider);
        
        // 도메인 서비스를 통한 사용자 처리 (FCM 토큰 없이)
        User user = oAuthDomainService.processOAuthUser(userInfo, provider, null);
        
        // 로그인 기록
        loginHistoryService.recordLogin(user.getId());
        
        return user;
    }
    
    /**
     * 제공자에 맞는 전략 가져오기
     */
    private OAuthStrategy getStrategy(AuthProvider provider) {
        OAuthStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            log.error("No strategy found for provider: {}", provider);
            throw new AuthException(ErrorMessage.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
        return strategy;
    }
}