package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.service.FCMTokenService;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.dto.LoginRequest;
import com.example.cherrydan.oauth.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * OAuth 도메인 서비스
 * 사용자 생성, 업데이트 등 도메인 로직을 담당
 * DDD의 Domain Service 패턴 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthDomainService {
    
    private final UserRepository userRepository;
    private final FCMTokenService fcmTokenService;
    
    /**
     * OAuth 사용자 처리 (찾기 또는 생성)
     * @param userInfo OAuth 제공자로부터 받은 사용자 정보
     * @param provider OAuth 제공자
     * @param loginRequest 로그인 요청 정보 (FCM 토큰 등 포함)
     * @return 처리된 사용자 엔티티
     */
    public User processOAuthUser(OAuth2UserInfo userInfo, AuthProvider provider, LoginRequest loginRequest) {
        // 1. 이메일 검증
        validateEmail(userInfo);
        
        // 2. 사용자 조회 또는 생성
        User user = findOrCreateUser(userInfo, provider);
        
        // 3. FCM 토큰 처리 (loginRequest가 있는 경우만)
        if (loginRequest != null) {
            registerDeviceInfo(user.getId(), loginRequest);
        }
        
        log.info("OAuth user processed successfully: userId={}, provider={}", 
            user.getId(), provider);
        
        return user;
    }
    
    /**
     * 이메일 유효성 검증
     */
    private void validateEmail(OAuth2UserInfo userInfo) {
        if (!StringUtils.hasText(userInfo.getEmail())) {
            log.error("OAuth email not found or empty");
            throw new OAuth2AuthenticationProcessingException(ErrorMessage.OAUTH_EMAIL_NOT_FOUND);
        }
    }
    
    /**
     * 사용자 조회 또는 생성
     * 비즈니스 규칙:
     * 1. 이메일로 기존 사용자 조회
     * 2. 삭제된 사용자는 로그인 거부
     * 3. 다른 제공자로 가입된 경우 오류
     * 4. 기존 사용자면 정보 업데이트
     * 5. 신규 사용자면 생성
     */
    private User findOrCreateUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        String email = userInfo.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            
            // 삭제된 사용자 검증
            if (existingUser.isDeleted()) {
                log.warn("Deleted user attempted to login: email={}", email);
                throw new OAuth2AuthenticationProcessingException(ErrorMessage.OAUTH_USER_DELETED);
            }
            
            // 제공자 일치 검증
            validateProvider(existingUser, provider);
            
            // 기존 사용자 정보 업데이트
            return updateExistingUser(existingUser, userInfo);
        } else {
            // 신규 사용자 생성
            return createNewUser(userInfo, provider);
        }
    }
    
    /**
     * OAuth 제공자 일치 검증
     */
    private void validateProvider(User user, AuthProvider provider) {
        if (user.getProvider() != null && !user.getProvider().equals(provider)) {
            String errorMessage = String.format(
                "이미 %s 계정으로 가입되어 있습니다. %s 계정으로 로그인해 주세요.",
                user.getProvider(), user.getEmail()
            );
            log.error("Provider mismatch: expected={}, actual={}", user.getProvider(), provider);
            throw new OAuth2AuthenticationProcessingException(errorMessage);
        }
    }
    
    /**
     * 신규 사용자 생성
     * Rich Domain Model 패턴 적용
     */
    private User createNewUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .picture(userInfo.getImageUrl())
                .socialId(userInfo.getId())
                .role(Role.ROLE_USER)
                .provider(provider)
                .build();
        
        log.info("Creating new OAuth user: email={}, provider={}", 
            newUser.getEmail(), provider);
        
        return userRepository.save(newUser);
    }
    
    /**
     * 기존 사용자 정보 업데이트
     * User 엔티티의 도메인 메서드 활용
     */
    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        // User 엔티티의 도메인 메서드 호출
        user.updateOAuth2Info(userInfo.getName(), userInfo.getImageUrl());
        
        log.info("Updating existing OAuth user: userId={}, email={}", 
            user.getId(), user.getEmail());
        
        return userRepository.save(user);
    }
    
    /**
     * 디바이스 정보 및 FCM 토큰 등록
     * 실패해도 로그인은 계속 진행
     */
    private void registerDeviceInfo(Long userId, LoginRequest loginRequest) {
        try {
            String fcmToken = loginRequest.getFcmToken();
            
            FCMTokenRequest fcmRequest = FCMTokenRequest.builder()
                    .userId(userId)
                    .fcmToken(fcmToken != null && !fcmToken.trim().isEmpty() ? fcmToken : null)
                    .deviceType(loginRequest.getDeviceType())
                    .deviceModel(loginRequest.getDeviceModel())
                    .appVersion(loginRequest.getAppVersion())
                    .osVersion(loginRequest.getOsVersion())
                    .isAllowed(loginRequest.getIsAllowed())
                    .build();
            
            fcmTokenService.registerOrUpdateToken(fcmRequest);
            
            log.info("Device info registered: userId={}, deviceType={}", 
                userId, loginRequest.getDeviceType());
            
        } catch (Exception e) {
            log.warn("Failed to register device info (login continues): userId={}, error={}", 
                userId, e.getMessage());
        }
    }
}