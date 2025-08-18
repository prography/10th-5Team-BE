package com.example.cherrydan.fcm.dto;

import lombok.*;

/**
 * FCM 토큰 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMTokenUpdateRequest {
    private Long deviceId;
    private String fcmToken;
    private Boolean isActive;
    private Boolean isAllowed;
}