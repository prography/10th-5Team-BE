package com.example.cherrydan.fcm.domain;

/**
 * FCM 디바이스 타입 열거형
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
public enum DeviceType {
    ANDROID("android"),
    IOS("ios"),
    WEB("web");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
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
