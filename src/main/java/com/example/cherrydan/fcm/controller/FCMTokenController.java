package com.example.cherrydan.fcm.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.service.FCMTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FCM 토큰 관리 컨트롤러
 * FCM 토큰 등록, 수정, 삭제 관련 API 엔드포인트를 제공하는 컨트롤러
 */
@Tag(name = "FCM Token Management", description = "FCM 토큰 등록/삭제/조회 관리 API - 클라이언트에서 토큰을 등록하고 관리할 때 사용해요")
@Slf4j
@RestController
@RequestMapping("/api/fcm/tokens")
@RequiredArgsConstructor
public class FCMTokenController {
    
    private final FCMTokenService fcmTokenService;
    
    /**
     * FCM 토큰 등록 또는 업데이트
     * 
     * @param request FCM 토큰 등록 요청
     * @return 등록/업데이트 결과
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> registerToken(@RequestBody FCMTokenRequest request) {
        
        log.info("FCM 토큰 등록/업데이트 요청 - 사용자: {}, 디바이스: {}", 
                request.getUserId(), request.getDeviceType());
        
        Long tokenId = fcmTokenService.registerOrUpdateToken(request);
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 등록되었습니다.", tokenId));
    }
    
    /**
     * 사용자의 모든 FCM 토큰 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자의 활성화된 토큰 리스트
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UserFCMToken>>> getUserTokens(@PathVariable Long userId) {
        
        log.info("사용자 {}의 FCM 토큰 조회", userId);
        
        List<UserFCMToken> tokens = fcmTokenService.getUserTokens(userId);
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰 조회가 완료되었습니다.", tokens));
    }
    
    /**
     * 특정 FCM 토큰 삭제
     * 
     * @param userId 사용자 ID
     * @param token 삭제할 FCM 토큰
     * @return 삭제 결과
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteToken(
            @PathVariable Long userId,
            @RequestParam String token) {
        
        log.info("FCM 토큰 삭제 요청 - 사용자: {}", userId);
        
        fcmTokenService.deleteToken(userId, token);
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 삭제되었습니다."));
    }
    
    /**
     * 사용자의 모든 FCM 토큰 삭제
     * 
     * @param userId 사용자 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/users/{userId}/all")
    public ResponseEntity<ApiResponse<String>> deleteAllTokens(@PathVariable Long userId) {
        
        log.info("사용자 {}의 모든 FCM 토큰 삭제 요청", userId);
        
        fcmTokenService.deleteAllUserTokens(userId);
        return ResponseEntity.ok(ApiResponse.success("모든 FCM 토큰이 삭제되었습니다."));
    }
    
    /**
     * FCM 토큰 유효성 확인
     * 
     * @param token 확인할 FCM 토큰
     * @return 토큰 유효성 여부
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        
        boolean isValid = fcmTokenService.isTokenValid(token);
        return ResponseEntity.ok(ApiResponse.success("토큰 유효성 확인이 완료되었습니다.", isValid));
    }
    
    /**
     * 사용자의 활성 토큰 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 활성 토큰 존재 여부
     */
    @GetMapping("/users/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveTokens(@PathVariable Long userId) {
        
        boolean hasTokens = fcmTokenService.hasActiveTokens(userId);
        return ResponseEntity.ok(ApiResponse.success("활성 토큰 존재 여부 확인이 완료되었습니다.", hasTokens));
    }
    
    /**
     * 수동으로 오래된 토큰 정리 실행 (관리자용)
     * 
     * @return 정리 결과
     */
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupOldTokens() {
        
        log.info("수동 토큰 정리 실행");
        
        fcmTokenService.cleanupOldTokens();
        return ResponseEntity.ok(ApiResponse.success("오래된 토큰 정리가 완료되었습니다."));
    }
}
