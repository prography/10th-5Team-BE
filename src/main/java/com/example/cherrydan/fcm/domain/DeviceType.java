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
     * 문자열로부터 DeviceType을 반환합니다.
     * @param value 디바이스 타입 문자열 (대소문자 무관)
     * @return 해당하는 DeviceType
     * @throws IllegalArgumentException 지원하지 않는 디바이스 타입인 경우
     */
    public static DeviceType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("디바이스 타입이 비어있습니다.");
        }

        for (DeviceType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 디바이스 타입입니다: " + value);
    }

    /**
     * 디바이스 타입이 유효한지 확인합니다.
     * @param value 디바이스 타입 문자열
     * @return 유효 여부
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        for (DeviceType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated from() 메소드를 사용하세요.
     */
    @Deprecated
    public static DeviceType fromString(String value) {
        return from(value);
    }
}
