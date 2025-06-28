package com.example.cherrydan.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Apple 로그인 요청")
public class AppleLoginRequest {
    
    @Schema(description = "Apple Identity Token (JWT)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String accessToken;
    
    @Schema(description = "FCM 토큰", example = "fcm_token_here")
    private String fcmToken;
    
    @Schema(description = "디바이스 타입", example = "ios")
    private String deviceType;
}
