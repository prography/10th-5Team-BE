package com.example.cherrydan.fcm.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.dto.MultipleNotificationRequest;
import com.example.cherrydan.fcm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FCM 알림 전송 컨트롤러
 * 푸시 알림 전송 관련 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 단일 사용자에게 알림 전송
     * 
     * @param userId 사용자 ID
     * @param request 알림 요청 데이터
     * @return 알림 전송 결과
     */
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendNotificationToUser(
            @PathVariable("userId") Long userId,
            @RequestBody NotificationRequest request) {
        
        log.info("사용자 {}에게 알림 전송 요청: {}", userId, request.getTitle());
        
        NotificationResultDto result = notificationService.sendNotificationToUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 여러 사용자에게 알림 전송
     * 
     * @param requestBody 사용자 ID 리스트와 알림 요청 데이터
     * @return 알림 전송 결과
     */
    @PostMapping("/users/multiple")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendNotificationToUsers(
            @RequestBody MultipleNotificationRequest requestBody) {
        
        List<Long> userIds = requestBody.getUserIds();
        NotificationRequest request = requestBody.getNotification();
        
        log.info("여러 사용자 {}명에게 알림 전송 요청: {}", userIds.size(), request.getTitle());
        
        NotificationResultDto result = notificationService.sendNotificationToUsers(userIds, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 토픽으로 알림 전송
     * 
     * @param topic 토픽 이름
     * @param request 알림 요청 데이터
     * @return 알림 전송 결과
     */
    @PostMapping("/topics/{topic}")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendNotificationToTopic(
            @PathVariable("topic") String topic,
            @RequestBody NotificationRequest request) {
        
        log.info("토픽 {}에 알림 전송 요청: {}", topic, request.getTitle());
        
        NotificationResultDto result = notificationService.sendNotificationToTopic(topic, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 특정 FCM 토큰으로 직접 알림 전송
     * 
     * @param token FCM 토큰
     * @param request 알림 요청 데이터
     * @return 알림 전송 결과
     */
    @PostMapping("/tokens/{token}")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendNotificationToToken(
            @PathVariable("token") String token,
            @RequestBody NotificationRequest request) {
        
        log.info("FCM 토큰으로 직접 알림 전송: {}", request.getTitle());
        
        NotificationResultDto result = notificationService.sendNotificationToToken(token, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    
    /**
     * 간단한 알림 전송 (제목과 내용만)
     * 
     * @param userId 사용자 ID
     * @param requestBody 제목과 내용
     * @return 알림 전송 결과
     */
    @PostMapping("/users/{userId}/simple")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendSimpleNotification(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, String> requestBody) {
        
        String title = requestBody.get("title");
        String body = requestBody.get("body");
        
        NotificationRequest request = NotificationRequest.createSimple(title, body);
        NotificationResultDto result = notificationService.sendNotificationToUser(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 브로드캐스트 알림 전송 (모든 사용자)
     * 
     * @param request 알림 요청 데이터
     * @return 알림 전송 결과
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendBroadcastNotification(
            @RequestBody NotificationRequest request) {
        
        log.info("브로드캐스트 알림 전송 요청: {}", request.getTitle());
        
        NotificationResultDto result = notificationService.sendNotificationToTopic("all-users", request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 테스트 알림 전송
     * 
     * @param userId 사용자 ID
     * @return 알림 전송 결과
     */
    @PostMapping("/users/{userId}/test")
    public ResponseEntity<ApiResponse<NotificationResultDto>> sendTestNotification(@PathVariable("userId") Long userId) {
        
        log.info("사용자 {}에게 테스트 알림 전송", userId);
        
        NotificationRequest testRequest = NotificationRequest.createSimple(
                "테스트 알림", 
                "알림 시스템이 정상적으로 작동하고 있습니다."
        );
        
        NotificationResultDto result = notificationService.sendNotificationToUser(userId, testRequest);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
