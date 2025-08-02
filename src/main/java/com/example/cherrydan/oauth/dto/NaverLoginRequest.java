package com.example.cherrydan.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Naver 로그인 요청 DTO")
public class NaverLoginRequest implements LoginRequest {
    
    @Schema(description = "Naver 액세스 토큰", example = "naver_access_token_string")
    private String accessToken;
    
    @Schema(description = "FCM 토큰", example = "fcm_token_here")
    private String fcmToken;
    
    @Schema(description = "디바이스 타입", example = "android")
    private String deviceType;

    @Schema(description = "디바이스 모델명", example = "iPhone 14 Pro")
    private String deviceModel;

    @Schema(description = "OS 버전", example = "16.5.1")
    private String osVersion;

    @Schema(description = "앱 버전", example = "1.0.0")
    private String appVersion;
} 