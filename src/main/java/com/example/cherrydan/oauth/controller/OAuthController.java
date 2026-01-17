package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.dto.*;
import com.example.cherrydan.oauth.service.OAuthFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 통합 OAuth 인증 컨트롤러
 * 기존 API 경로를 유지하면서 내부 구조만 개선
 * 각 제공자별 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "OAuth 인증", description = "OAuth 로그인 통합 API")
public class OAuthController {
    
    private final OAuthFacade oAuthFacade;
    
    /**
     * Kakao 로그인 - 기존 경로 유지
     */
    @PostMapping("/kakao/login")
    @Operation(summary = "Kakao 로그인", description = "Kakao OAuth 로그인 처리")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @RequestBody KakaoLoginRequest request) {
        log.info("Kakao login request received");
        LoginResponse response = oAuthFacade.processOAuthLogin(AuthProvider.KAKAO, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Google 로그인 - 기존 경로 유지
     */
    @PostMapping("/google/login")
    @Operation(summary = "Google 로그인", description = "Google OAuth 로그인 처리")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
            @RequestBody GoogleLoginRequest request) {
        log.info("Google login request received");
        LoginResponse response = oAuthFacade.processOAuthLogin(AuthProvider.GOOGLE, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Naver 로그인 - 기존 경로 유지
     */
    @PostMapping("/naver/login")
    @Operation(summary = "Naver 로그인", description = "Naver OAuth 로그인 처리")
    public ResponseEntity<ApiResponse<LoginResponse>> naverLogin(
            @RequestBody NaverLoginRequest request) {
        log.info("Naver login request received");
        LoginResponse response = oAuthFacade.processOAuthLogin(AuthProvider.NAVER, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Apple 로그인 - 기존 경로 유지
     */
    @PostMapping("/apple/login")
    @Operation(summary = "Apple 로그인", description = "Apple OAuth 로그인 처리")
    public ResponseEntity<ApiResponse<LoginResponse>> appleLogin(
            @RequestBody AppleLoginRequest request) {
        log.info("Apple login request received");
        LoginResponse response = oAuthFacade.processOAuthLogin(AuthProvider.APPLE, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}