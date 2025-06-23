package com.example.cherrydan.inquiry.domain;

import lombok.Getter;

@Getter
public enum InquiryCategory {
    USAGE("이용관련"),
    ACCOUNT("계정"),
    COMPLAINT("불편한 사항"),
    OTHER("기타");

    private final String description;

    InquiryCategory(String description) {
        this.description = description;
    }

    /**
     * 문자열로부터 InquiryCategory를 반환합니다.
     * @param value 카테고리 문자열 (대소문자 무관)
     * @return 해당하는 InquiryCategory
     * @throws IllegalArgumentException 지원하지 않는 카테고리인 경우
     */
    public static InquiryCategory from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("문의 카테고리가 비어있습니다.");
        }

        for (InquiryCategory category : values()) {
            if (category.name().equalsIgnoreCase(value.trim()) ||
                    category.description.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 문의 카테고리입니다: " + value);
    }

    /**
     * 카테고리가 유효한지 확인합니다.
     * @param value 카테고리 문자열
     * @return 유효 여부
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        for (InquiryCategory category : values()) {
            if (category.name().equalsIgnoreCase(value.trim()) ||
                    category.description.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated from() 메소드를 사용하세요.
     */
    @Deprecated
    public static InquiryCategory fromString(String value) {
        return from(value);
    }
}