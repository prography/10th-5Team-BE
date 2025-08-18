package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.FCMException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.dto.FCMTokenResponseDTO;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
     * 사용자의 모든 활성화된 FCM 토큰 조회
     */
    public List<FCMTokenResponseDTO> getUserFCMTokens(Long userId) {
        List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserId(userId);
        return tokens.stream()
                .map(FCMTokenResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 알림을 허용하는 디바이스가 있는지 확인
     * @param userId 사용자 ID
     * @return 알림 허용 디바이스가 하나라도 있으면 true, 없으면 false
     */
    public boolean hasNotificationAllowedDevice(Long userId) {
        List<UserFCMToken> allowedTokens = tokenRepository.findActiveTokensByUserId(userId);
        return !allowedTokens.isEmpty();
    }

    /**
     * FCM 토큰 등록 또는 업데이트
     */
    @Transactional
    public void registerOrUpdateToken(FCMTokenRequest request) {
        try {
            
            Optional<UserFCMToken> existingToken = tokenRepository
                    .findByUserIdAndDeviceModelAndIsActiveTrue(request.getUserId(), request.getDeviceModel());
            
            UserFCMToken token;
            if (existingToken.isPresent()) {
                token = existingToken.get();
                token.updateToken(request);
                token.activate();
                log.info("FCM 토큰 업데이트 - 사용자: {}, 디바이스: {}", request.getUserId(), request.getDeviceModel());
            } else {
                token = UserFCMToken.builder()
                        .userId(request.getUserId())
                        .fcmToken(request.getFcmToken())
                        .isActive(true)
                        .isAllowed(request.getIsAllowed() != null ? request.getIsAllowed() : true)
                        .deviceType(DeviceType.from(request.getDeviceType()))
                        .deviceModel(request.getDeviceModel())
                        .appVersion(request.getAppVersion())
                        .osVersion(request.getOsVersion())
                        .build();
                tokenRepository.save(token);
                log.info("새 FCM 토큰 등록 - 사용자: {}, 디바이스: {}", request.getUserId(), request.getDeviceModel());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 디바이스 타입: {}", request.getDeviceType());
        } catch (FCMException e) {
            log.error("FCM 토큰 등록/업데이트 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("서버 내부 에러 발생 {}", e.getMessage());
        }
    }
}
