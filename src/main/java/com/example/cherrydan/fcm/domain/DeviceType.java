package com.example.cherrydan.fcm.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * FCM 디바이스 타입 열거형
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
@Getter
@Schema(description = "FCM 디바이스 타입", example = "ANDROID")
public enum DeviceType {
    
    @Schema(description = "안드로이드 기기에서 사용해야 해요")
    ANDROID("android"),
    
    @Schema(description = "iOS 기기에서 사용해야 해요")
    IOS("ios"),
    
    @Schema(description = "웹 브라우저에서 사용해야 해요")
    WEB("web");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    /**
     * 문자열 값으로부터 DeviceType 반환
     * 
     * @param value 디바이스 타입 문자열 (대소문자 무관)
     * @return 해당하는 DeviceType
     * @throws IllegalArgumentException 지원하지 않는 디바이스 타입인 경우
     */
    public static DeviceType fromString(String value) {
        for (DeviceType type : DeviceType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid device type: " + value);
    }
}
