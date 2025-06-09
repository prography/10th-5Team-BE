package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.NotificationException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 알림 전송 서비스
 * FCM을 통한 푸시 알림 전송을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final UserFCMTokenRepository tokenRepository;
    
    /**
     * 단일 사용자에게 알림 전송
     */
    public NotificationResultDto sendNotificationToUser(Long userId, NotificationRequest request) {
        if (!isFirebaseInitialized()) {
            log.error("Firebase가 초기화되지 않았습니다. FCM 기능을 사용할 수 없습니다.");
            throw new NotificationException(ErrorMessage.NOTIFICATION_SERVICE_UNAVAILABLE);
        }
        
        try {
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserId(userId);
            
            if (tokens.isEmpty()) {
                log.error("사용자 {}의 활성화된 FCM 토큰이 없습니다.", userId);
                throw new NotificationException(ErrorMessage.NOTIFICATION_USER_NO_TOKENS);
            }
            
            List<String> tokenStrings = tokens.stream()
                    .map(UserFCMToken::getFcmToken)
                    .collect(Collectors.toList());
            
            return sendMulticastNotification(tokenStrings, request, tokens);
            
        } catch (NotificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 {}에게 알림 전송 실패: {}", userId, e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_SEND_FAILED);
        }
    }
    
    /**
     * 여러 사용자에게 알림 전송
     */
    public NotificationResultDto sendNotificationToUsers(List<Long> userIds, NotificationRequest request) {
        try {
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserIds(userIds);
            
            if (tokens.isEmpty()) {
                log.error("지정된 사용자들의 활성화된 FCM 토큰이 없습니다.");
                throw new NotificationException(ErrorMessage.NOTIFICATION_USER_NO_TOKENS);
            }
            
            List<String> tokenStrings = tokens.stream()
                    .map(UserFCMToken::getFcmToken)
                    .collect(Collectors.toList());
            
            return sendMulticastNotification(tokenStrings, request, tokens);
            
        } catch (NotificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("여러 사용자에게 알림 전송 실패: {}", e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_MULTIPLE_SEND_FAILED);
        }
    }
    
    /**
     * 토픽을 통한 알림 전송
     * 이 부분은 프론트와 협의해서 토픽을 서버에 저장할 지 고민
     */
    public NotificationResultDto sendNotificationToTopic(String topic, NotificationRequest request) {
        try {
            Message message = Message.builder()
                    .setNotification(buildNotification(request))
                    .putAllData(request.getData())
                    .setTopic(topic)
                    .setAndroidConfig(buildAndroidConfig(request))
                    .setApnsConfig(buildApnsConfig(request))
                    .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("토픽 {} 알림 전송 성공: {}", topic, response);
            
            return NotificationResultDto.topicResult(topic);
            
        } catch (FirebaseMessagingException e) {
            log.error("토픽 {} 알림 전송 실패: {}", topic, e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_TOPIC_SEND_FAILED);
        }
    }
    
    /**
     * 특정 FCM 토큰으로 직접 알림 전송
     */
    public NotificationResultDto sendNotificationToToken(String fcmToken, NotificationRequest request) {
        try {
            Message message = Message.builder()
                    .setNotification(buildNotification(request))
                    .putAllData(request.getData())
                    .setToken(fcmToken)
                    .setAndroidConfig(buildAndroidConfig(request))
                    .setApnsConfig(buildApnsConfig(request))
                    .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 토큰 알림 전송 성공: {}", response);
            
            updateTokenLastUsed(fcmToken);
            
            return NotificationResultDto.singleSuccess();
            
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토큰 알림 전송 실패: {}", e.getMessage());
            
            if (isInvalidTokenError(e)) {
                deactivateToken(fcmToken);
            }
            
            throw new NotificationException(ErrorMessage.NOTIFICATION_TOKEN_SEND_FAILED);
        }
    }
    
    /**
     * 다중 토큰으로 알림 전송 (내부 메서드)
     */
    private NotificationResultDto sendMulticastNotification(List<String> tokens, NotificationRequest request, List<UserFCMToken> tokenEntities) {
        try {
            List<List<String>> tokenChunks = chunkList(tokens, 500);
            int totalSuccess = 0;
            int totalFailure = 0;
            List<String> invalidTokens = new ArrayList<>();
            
            for (List<String> chunk : tokenChunks) {
                MulticastMessage message = MulticastMessage.builder()
                        .setNotification(buildNotification(request))
                        .putAllData(request.getData())
                        .addAllTokens(chunk)
                        .setAndroidConfig(buildAndroidConfig(request))
                        .setApnsConfig(buildApnsConfig(request))
                        .build();
                
                BatchResponse batchResponse = FirebaseMessaging.getInstance().sendMulticast(message);
                totalSuccess += batchResponse.getSuccessCount();
                totalFailure += batchResponse.getFailureCount();
                
                processFailedTokens(batchResponse, chunk, invalidTokens);
            }
            
            updateSuccessfulTokensLastUsed(tokenEntities, invalidTokens);
            deactivateInvalidTokens(invalidTokens);
            
            log.info("알림 전송 완료 - 성공: {}, 실패: {}, 무효 토큰: {}", 
                    totalSuccess, totalFailure, invalidTokens.size());
            
            return NotificationResultDto.multipleResult(totalSuccess, totalFailure, 
                    String.format("성공: %d, 실패: %d", totalSuccess, totalFailure));
            
        } catch (FirebaseMessagingException e) {
            log.error("다중 알림 전송 실패: {}", e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_MULTIPLE_SEND_FAILED);
        }
    }
    
    /**
     * 알림 객체 생성
     */
    private Notification buildNotification(NotificationRequest request) {
        return Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .setImage(request.getImageUrl())
                .build();
    }
    
    /**
     * Android 설정 생성
     */
    private AndroidConfig buildAndroidConfig(NotificationRequest request) {
        return AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                        .setIcon(request.getAndroidIcon())
                        .setColor(request.getAndroidColor())
                        .setSound(request.getAndroidSound())
                        .setClickAction(request.getClickAction())
                        .build())
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();
    }
    
    /**
     * iOS APNS 설정 생성
     */
    private ApnsConfig buildApnsConfig(NotificationRequest request) {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .build())
                        .setSound(request.getIosSound())
                        .setBadge(request.getIosBadge())
                        .setCategory(request.getIosCategory())
                        .build())
                .build();
    }
    
    /**
     * 실패한 토큰들 처리
     */
    private void processFailedTokens(BatchResponse batchResponse, List<String> tokens, List<String> invalidTokens) {
        List<SendResponse> responses = batchResponse.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (!response.isSuccessful()) {
                String token = tokens.get(i);
                FirebaseMessagingException exception = response.getException();
                
                if (isInvalidTokenError(exception)) {
                    invalidTokens.add(token);
                    log.error("무효한 토큰 발견: {}", token);
                } else {
                    log.error("토큰 {} 전송 실패: {}", token, exception.getMessage());
                }
            }
        }
    }
    
    /**
     * 무효한 토큰 에러인지 확인
     */
    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
               e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED;
    }
    
    /**
     * 성공한 토큰들의 마지막 사용 시간 업데이트
     */
    private void updateSuccessfulTokensLastUsed(List<UserFCMToken> tokenEntities, List<String> invalidTokens) {
        tokenEntities.stream()
                .filter(token -> !invalidTokens.contains(token.getFcmToken()))
                .forEach(UserFCMToken::updateLastUsed);
    }
    
    /**
     * 무효한 토큰들 비활성화
     */
    public void deactivateInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            deactivateToken(token);
        }
    }
    
    /**
     * 특정 토큰 비활성화
     */
    public void deactivateToken(String fcmToken) {
        tokenRepository.deactivateByFcmToken(fcmToken);
        log.info("FCM 토큰 비활성화: {}", fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...");
    }
    
    /**
     * 토큰 마지막 사용 시간 업데이트
     */
    public void updateTokenLastUsed(String fcmToken) {
        tokenRepository.findByFcmToken(fcmToken)
                .ifPresent(UserFCMToken::updateLastUsed);
    }
    
    /**
     * 리스트를 지정된 크기로 분할
     */
    private <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }
    
    /**
     * Firebase 초기화 상태 확인
     */
    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }
}
