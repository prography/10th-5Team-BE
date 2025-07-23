package com.example.cherrydan.user.domain;

public enum Gender {
    MALE,    // 남성
    FEMALE;  // 여성

    /**
     * 문자열로부터 Gender를 반환합니다.
     * @param value 성별 문자열 (대소문자 무관)
     * @return 해당하는 Gender
     * @throws IllegalArgumentException 지원하지 않는 성별인 경우
     */
    public static Gender from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("성별이 비어있습니다.");
        }

        for (Gender gender : values()) {
            if (gender.name().equalsIgnoreCase(value.trim())) {
                return gender;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 성별입니다: " + value);
    }
} 