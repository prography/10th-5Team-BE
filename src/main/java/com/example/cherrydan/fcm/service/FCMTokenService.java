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
    public Long registerOrUpdateToken(FCMTokenRequest request) {
        try {
            Long userId = request.getUserId();
            String fcmToken = request.getFcmToken();
            DeviceType deviceType = DeviceType.from(request.getDeviceType());
            
            if (!isValidTokenRequest(request)) {
                throw new FCMException(ErrorMessage.FCM_TOKEN_INVALID_REQUEST);
            }
            
            Optional<UserFCMToken> existingToken = tokenRepository
                    .findByUserIdAndDeviceTypeAndIsActiveTrue(userId, deviceType);
            
            if (existingToken.isPresent()) {
                UserFCMToken token = existingToken.get();
                token.updateToken(fcmToken);
                token.activate();
                
                log.info("FCM 토큰 업데이트 - 사용자: {}, 디바이스: {}", userId, deviceType);
                return token.getId();
            } else {
                UserFCMToken newToken = UserFCMToken.builder()
                        .userId(userId)
                        .fcmToken(fcmToken)
                        .isActive(true)
                        .deviceType(deviceType)
                        .build();
                
                UserFCMToken savedToken = tokenRepository.save(newToken);
                
                log.info("새 FCM 토큰 등록 - 사용자: {}, 디바이스: {}", userId, deviceType);
                return savedToken.getId();
            }
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 디바이스 타입: {}", request.getDeviceType());
            throw new FCMException(ErrorMessage.FCM_DEVICE_TYPE_INVALID);
        } catch (FCMException e) {
            throw e;
        } catch (Exception e) {
            log.error("FCM 토큰 등록/업데이트 실패: {}", e.getMessage());
            throw new FCMException(ErrorMessage.FCM_TOKEN_REGISTRATION_FAILED);
        }
    }
    
    /**
     * 오래된 토큰 정리 (스케줄링)
     * 90일 이상 사용되지 않은 토큰들을 비활성화
     */
    @Scheduled(cron = "0 0 2 * * ?",zone = "Asia/Seoul")
    @Transactional
    public void cleanupOldTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<UserFCMToken> oldTokens = tokenRepository.findTokensNotUsedSince(cutoffDate);

        if (!oldTokens.isEmpty()) {
            tokenRepository.deleteAll(oldTokens);  // 진짜 삭제
            log.info("오래된 FCM 토큰 삭제 완료 - 삭제된 토큰 수: {}", oldTokens.size());
        }
    }
    
    /**
     * 토큰 요청 유효성 검사
     */
    private boolean isValidTokenRequest(FCMTokenRequest request) {
        return request != null &&
               request.getUserId() != null &&
               request.getFcmToken() != null && !request.getFcmToken().trim().isEmpty() &&
               request.getDeviceType() != null && !request.getDeviceType().trim().isEmpty();
    }
}
