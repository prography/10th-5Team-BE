package com.example.cherrydan.user.domain;

/**
 * 사용자 역할 열거형
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    /**
     * 문자열로부터 Role을 반환합니다.
     * @param value 역할 문자열 (대소문자 무관)
     * @return 해당하는 Role
     * @throws IllegalArgumentException 지원하지 않는 역할인 경우
     */
    public static Role from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("역할이 비어있습니다.");
        }

        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 역할입니다: " + value);
    }

    /**
     * 역할이 유효한지 확인합니다.
     * @param value 역할 문자열
     * @return 유효 여부
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }
}
