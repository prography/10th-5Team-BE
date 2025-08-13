package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.FCMException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * FCM 토큰 등록 또는 업데이트
     */
    @Transactional
    public void registerOrUpdateToken(FCMTokenRequest request) {
        try {
            
            DeviceType deviceType = DeviceType.from(request.getDeviceType());
            Optional<UserFCMToken> existingToken = tokenRepository
                    .findByUserIdAndDeviceTypeAndIsActiveTrue(request.getUserId(), deviceType);
            
            UserFCMToken token;
            if (existingToken.isPresent()) {
                token = existingToken.get();
                token.updateToken(request);
                token.activate();
                log.info("FCM 토큰 업데이트 - 사용자: {}, 디바이스: {}", request.getUserId(), deviceType);
            } else {
                token = UserFCMToken.builder()
                        .userId(request.getUserId())
                        .fcmToken(request.getFcmToken())
                        .isActive(true)
                        .deviceType(deviceType)
                        .deviceModel(request.getDeviceModel())
                        .appVersion(request.getAppVersion())
                        .osVersion(request.getOsVersion())
                        .build();
                tokenRepository.save(token);
                log.info("새 FCM 토큰 등록 - 사용자: {}, 디바이스: {}", request.getUserId(), deviceType);
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
