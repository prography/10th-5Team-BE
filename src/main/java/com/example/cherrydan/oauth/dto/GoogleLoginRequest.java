package com.example.cherrydan.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Google 모바일 로그인 요청 Body")
public class GoogleLoginRequest {
    
    @Schema(description = "Google Sign-In SDK로부터 발급받은 ID Token 문자열", 
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzRhYmM1Njc4Z...", 
            required = true)
    private String accessToken;
    
    @Schema(description = "FCM 토큰", example = "fcm_token_here")
    private String fcmToken;
    
    @Schema(description = "디바이스 타입", example = "android")
    private String deviceType;
} 