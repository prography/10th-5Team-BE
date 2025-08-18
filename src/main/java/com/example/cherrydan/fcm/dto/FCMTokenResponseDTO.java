package com.example.cherrydan.fcm.dto;

import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FCM 토큰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMTokenResponseDTO {
    private Long deviceId;
    private String fcmToken;
    private DeviceType deviceType;
    private String deviceModel;
    private String appVersion;
    private String osVersion;
    private Boolean isActive;
    private Boolean isAllowed;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FCMTokenResponseDTO from(UserFCMToken token) {
        return FCMTokenResponseDTO.builder()
                .deviceId(token.getId())
                .fcmToken(token.getFcmToken())
                .deviceType(token.getDeviceType())
                .deviceModel(token.getDeviceModel())
                .appVersion(token.getAppVersion())
                .osVersion(token.getOsVersion())
                .isActive(token.getIsActive())
                .isAllowed(token.getIsAllowed())
                .lastUsedAt(token.getLastUsedAt())
                .createdAt(token.getCreatedAt())
                .updatedAt(token.getUpdatedAt())
                .build();
    }
}