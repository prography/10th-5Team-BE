package com.example.cherrydan.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * FCM 토큰 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FCM 토큰 수정 요청")
public class FCMTokenUpdateRequest {
    @Schema(description = "디바이스 ID", example = "1", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deviceId;
    private String fcmToken;
    private Boolean isAllowed;
}