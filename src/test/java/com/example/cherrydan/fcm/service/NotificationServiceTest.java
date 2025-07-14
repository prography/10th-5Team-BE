package com.example.cherrydan.fcm.service;

import com.example.cherrydan.fcm.config.FirebaseConfig;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * NotificationService 단위 테스트 클래스
 * 실제 FCM 토큰으로 알림 전송을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private UserFCMTokenRepository tokenRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Firebase 초기화
        try {
            FirebaseConfig firebaseConfig = new FirebaseConfig();
            ReflectionTestUtils.setField(firebaseConfig, "serviceAccountKeyPath", "classpath:firebase-service-account-key.json");
            firebaseConfig.initializeFirebase();
            System.out.println("Firebase 초기화 성공");
        } catch (Exception e) {
            System.out.println("Firebase 초기화 실패: " + e.getMessage());
        }
    }

    @Test
    void testSendNotificationToToken() {
        // 실제 FCM 토큰
         String testFcmToken = "";
        
        
        // 테스트 알림 요청 생성
        NotificationRequest request = NotificationRequest.builder()
                .title("박우성")
                .body("박우성 바보")
                .data(Map.of(
                        "type", "test",
                        "action", "open_app",
                        "timestamp", String.valueOf(System.currentTimeMillis())
                ))
                .priority("high")
                .build();

        try {
            // FCM 토큰으로 직접 알림 전송
            NotificationResultDto result = notificationService.sendNotificationToToken(testFcmToken, request);
            
            System.out.println("=== FCM 단일 토큰 테스트 결과 ===");
            System.out.println("성공 여부: " + (result.getSuccessCount() > 0));
            System.out.println("성공 수: " + result.getSuccessCount());
            System.out.println("실패 수: " + result.getFailureCount());
            System.out.println("총 수: " + result.getTotalCount());
            System.out.println("상세 메시지: " + result.getDetails());
            System.out.println("완료 시간: " + result.getCompletedAt());
            
            if (result.getSuccessCount() > 0) {
                System.out.println("FCM 단일 토큰 알림 전송 성공!");
            } else {
                System.out.println("FCM 단일 토큰 알림 전송 실패!");
            }
            
        } catch (Exception e) {
            System.out.println("=== FCM 단일 토큰 테스트 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            System.out.println("에러 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    @Test
    void testSendMulticastNotification() {
        // 실제 FCM 토큰들 (여러 개의 토큰을 테스트)
        List<String> testFcmTokens = Arrays.asList(
            "",
            ""
        );
        
        // 테스트 알림 요청 생성
        NotificationRequest request = NotificationRequest.builder()
                .title("멀티캐스트 테스트 알림")
                .body("여러 기기에 동시 전송 테스트입니다!")
                .data(Map.of(
                        "type", "multicast_test",
                        "action", "open_app",
                        "timestamp", String.valueOf(System.currentTimeMillis())
                ))
                .priority("high")
                .build();

        try {
            // Multicast 알림 전송을 위한 내부 메서드 호출
            // Reflection을 사용하여 private 메서드 호출
            java.lang.reflect.Method sendMulticastMethod = NotificationService.class
                .getDeclaredMethod("sendMulticastNotification", List.class, NotificationRequest.class, List.class);
            sendMulticastMethod.setAccessible(true);
            
            // Mock UserFCMToken 리스트 생성
            List<com.example.cherrydan.fcm.domain.UserFCMToken> mockTokens = Arrays.asList(
                createMockUserFCMToken(1L, testFcmTokens.get(0)),
                createMockUserFCMToken(2L, testFcmTokens.get(1))
            );
            
            NotificationResultDto result = (NotificationResultDto) sendMulticastMethod.invoke(
                notificationService, testFcmTokens, request, mockTokens);
            
            System.out.println("=== FCM 멀티캐스트 테스트 결과 ===");
            System.out.println("전송 대상 토큰 수: " + testFcmTokens.size());
            System.out.println("성공 여부: " + (result.getSuccessCount() > 0));
            System.out.println("성공 수: " + result.getSuccessCount());
            System.out.println("실패 수: " + result.getFailureCount());
            System.out.println("총 수: " + result.getTotalCount());
            System.out.println("상세 메시지: " + result.getDetails());
            System.out.println("완료 시간: " + result.getCompletedAt());
            
            if (result.getSuccessCount() > 0) {
                System.out.println("FCM 멀티캐스트 알림 전송 성공!");
                System.out.println("성공한 사용자 ID: " + result.getSuccessfulUserIds());
            } else {
                System.out.println("FCM 멀티캐스트 알림 전송 실패!");
            }
            
        } catch (Exception e) {
            System.out.println("=== FCM 멀티캐스트 테스트 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            System.out.println("에러 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    @Test
    void testSendNotificationToUsers() {
        // 사용자 ID 리스트
        List<Long> userIds = Arrays.asList(1L, 2L);
        
        // Mock 설정 - UserFCMTokenRepository가 반환할 토큰들
        List<com.example.cherrydan.fcm.domain.UserFCMToken> mockTokens = Arrays.asList(
            createMockUserFCMToken(1L, ""),
            createMockUserFCMToken(2L, "")
        );
        
        // Mock 동작 설정
        org.mockito.Mockito.when(tokenRepository.findActiveTokensByUserIds(userIds))
            .thenReturn(mockTokens);
        
        // 테스트 알림 요청 생성
        NotificationRequest request = NotificationRequest.builder()
                .title("사용자별 알림 테스트")
                .body("특정 사용자들에게 전송하는 테스트입니다!")
                .data(Map.of(
                        "type", "user_notification_test",
                        "action", "open_app",
                        "timestamp", String.valueOf(System.currentTimeMillis())
                ))
                .priority("high")
                .build();

        try {
            // 사용자별 알림 전송
            NotificationResultDto result = notificationService.sendNotificationToUsers(userIds, request);
            
            System.out.println("=== FCM 사용자별 알림 테스트 결과 ===");
            System.out.println("전송 대상 사용자 수: " + userIds.size());
            System.out.println("사용자 ID: " + userIds);
            System.out.println("성공 여부: " + (result.getSuccessCount() > 0));
            System.out.println("성공 수: " + result.getSuccessCount());
            System.out.println("실패 수: " + result.getFailureCount());
            System.out.println("총 수: " + result.getTotalCount());
            System.out.println("상세 메시지: " + result.getDetails());
            System.out.println("완료 시간: " + result.getCompletedAt());
            
            if (result.getSuccessCount() > 0) {
                System.out.println("FCM 사용자별 알림 전송 성공!");
                System.out.println("성공한 사용자 ID: " + result.getSuccessfulUserIds());
            } else {
                System.out.println("FCM 사용자별 알림 전송 실패!");
            }
            
        } catch (Exception e) {
            System.out.println("=== FCM 사용자별 알림 테스트 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            System.out.println("에러 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    @Test
    void testFirebaseInitialization() {
        System.out.println("=== Firebase 초기화 상태 확인 ===");
        
        try {
            // Firebase 초기화 상태 확인
            boolean isInitialized = !com.google.firebase.FirebaseApp.getApps().isEmpty();
            System.out.println("Firebase 초기화 상태: " + (isInitialized ? "초기화됨" : "초기화되지 않음"));
            
            if (isInitialized) {
                com.google.firebase.FirebaseApp app = com.google.firebase.FirebaseApp.getInstance();
                System.out.println("Firebase 앱 이름: " + app.getName());
                System.out.println("Firebase 프로젝트 ID: " + app.getOptions().getProjectId());
            }
            
        } catch (Exception e) {
            System.out.println("Firebase 초기화 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mock UserFCMToken 생성 헬퍼 메서드
     */
    private com.example.cherrydan.fcm.domain.UserFCMToken createMockUserFCMToken(Long userId, String fcmToken) {
        return com.example.cherrydan.fcm.domain.UserFCMToken.builder()
                .id(1L)
                .userId(userId)
                .fcmToken(fcmToken)
                .deviceType(com.example.cherrydan.fcm.domain.DeviceType.ANDROID)
                .isActive(true)
                .build();
    }
} 