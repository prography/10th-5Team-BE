package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.FCMException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.dto.FCMTokenResponseDTO;
import com.example.cherrydan.fcm.dto.FCMTokenUpdateRequest;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FCM 토큰 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FCMTokenService {
    
    private final UserFCMTokenRepository tokenRepository;
    
    /**
     * 특정 디바이스의 FCM 토큰 수정
     * 기존 테스트용
     */
    @Transactional
    public void updateFCMToken(Long userId, Long deviceId, String newFcmToken) {
        try {
            UserFCMToken token = tokenRepository.findById(deviceId)
                .orElseThrow(() -> new FCMException(ErrorMessage.FCM_TOKEN_NOT_FOUND));
            
            if (!token.getUserId().equals(userId)) {
                throw new FCMException(ErrorMessage.FCM_TOKEN_ACCESS_DENIED);
            }
            
            token.updateFcmToken(newFcmToken);
            log.info("FCM 토큰 수정 완료 - 사용자: {}, 디바이스 ID: {}, 디바이스: {}", 
                    userId, deviceId, token.getDeviceType());
            
        } catch (FCMException e) {
            log.error("FCM 토큰 수정 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("FCM 토큰 수정 중 서버 내부 에러 발생: {}", e.getMessage());
            throw new FCMException(ErrorMessage.FCM_TOKEN_UPDATE_FAILED);
        }
    }

    /**
     * 특정 디바이스의 FCM 토큰 및 상태 수정
     */
    @Transactional
    public void updateFCMTokenWithStatus(Long userId, FCMTokenUpdateRequest request) {
        try {
            UserFCMToken token = tokenRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new FCMException(ErrorMessage.FCM_TOKEN_NOT_FOUND));
            
            if (!token.getUserId().equals(userId)) {
                throw new FCMException(ErrorMessage.FCM_TOKEN_ACCESS_DENIED);
            }
            
            // FCM 토큰 업데이트
            if (request.getFcmToken() != null) {
                token.updateFcmToken(request.getFcmToken());
            }
            
            // 알림 허용 상태 업데이트
            if (request.getIsAllowed() != null) {
                token.updateAllowedStatus(request.getIsAllowed());
            }
            
            log.info("FCM 토큰 및 상태 수정 완료 - 사용자: {}, 디바이스 ID: {}", userId, request.getDeviceId());
            
        } catch (FCMException e) {
            log.error("FCM 토큰 수정 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("FCM 토큰 수정 중 서버 내부 에러 발생: {}", e.getMessage());
            throw new FCMException(ErrorMessage.FCM_TOKEN_UPDATE_FAILED);
        }
    }

    /**
     * 사용자의 모든 FCM 토큰 조회 (활성화 및 알림 허용 여부 무관)
     */
    public List<FCMTokenResponseDTO> getUserFCMTokens(Long userId) {
        List<UserFCMToken> tokens = tokenRepository.findByUserId(userId);
        return tokens.stream()
                .map(FCMTokenResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * FCM 토큰 등록 또는 업데이트
     */
    @Transactional
    public void registerOrUpdateToken(FCMTokenRequest request) {
        try {

            Optional<UserFCMToken> existingToken = tokenRepository
                    .findByUserIdAndDeviceModelAndIsActiveTrue(request.getUserId(), request.getDeviceModel());

            UserFCMToken userFCMToken = existingToken
                    .orElseGet(() -> createInactiveFCMToken(request));

            boolean isNewToken = existingToken.isEmpty();
            if (!isNewToken) {
                userFCMToken.updateToken(request);
            }

            userFCMToken.activate();
            tokenRepository.save(userFCMToken);

            log.info("{} FCM 토큰 - 사용자: {}, 디바이스: {}",
                    isNewToken ? "새" : "업데이트",
                    request.getUserId(),
                    request.getDeviceModel());

        } catch (IllegalArgumentException e) {
            log.error("잘못된 디바이스 타입: {}", request.getDeviceType());
        } catch (FCMException e) {
            log.error("FCM 토큰 등록/업데이트 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("서버 내부 에러 발생 {}", e.getMessage());
        }
    }

    /**
     * 비활성 상태의 FCM 토큰 생성
     */
    private UserFCMToken createInactiveFCMToken(FCMTokenRequest request) {
        return UserFCMToken.builder()
                .userId(request.getUserId())
                .fcmToken(request.getFcmToken())
                .isActive(false)
                .isAllowed(request.getIsAllowed() != null ? request.getIsAllowed() : true)
                .deviceType(DeviceType.from(request.getDeviceType()))
                .deviceModel(request.getDeviceModel())
                .appVersion(request.getAppVersion())
                .osVersion(request.getOsVersion())
                .build();
    }

    /**
     * 사용자의 모든 FCM 토큰 비활성화 (소프트 삭제)
     * 사용자 탈퇴 시 호출
     */
    @Transactional
    public void deactivateUserTokens(Long userId) {
        List<UserFCMToken> tokens = tokenRepository.findByUserId(userId);
        tokens.forEach(UserFCMToken::deactivate);
        log.info("사용자 {}의 모든 FCM 토큰 비활성화 완료: {} 개", userId, tokens.size());
    }

    /**
     * 사용자의 모든 FCM 토큰 활성화
     * 사용자 복구 시 호출
     */
    @Transactional
    public void activateUserTokens(Long userId) {
        List<UserFCMToken> tokens = tokenRepository.findByUserId(userId);
        tokens.forEach(UserFCMToken::activate);
        log.info("사용자 {}의 모든 FCM 토큰 활성화 완료: {} 개", userId, tokens.size());
    }
}
