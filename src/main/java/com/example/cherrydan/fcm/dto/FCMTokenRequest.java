package com.example.cherrydan.fcm.dto;

import lombok.*;

/**
 * FCM 토큰 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMTokenRequest {
    private Long userId;
    private String fcmToken;
    private String deviceType; // "android" or "ios"
    private String deviceModel;
    private String appVersion;
    private String osVersion;
}
