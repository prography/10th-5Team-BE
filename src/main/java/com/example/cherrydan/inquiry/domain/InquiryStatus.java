package com.example.cherrydan.inquiry.domain;

import lombok.Getter;

@Getter
public enum InquiryStatus {
    PENDING("대기중"),
    PROCESSING("처리중"),
    COMPLETED("완료"),
    CLOSED("종료");

    private final String description;

    InquiryStatus(String description) {
        this.description = description;
    }

    /**
     * 문자열로부터 InquiryStatus를 반환합니다.
     * @param value 상태 문자열 (대소문자 무관)
     * @return 해당하는 InquiryStatus
     * @throws IllegalArgumentException 지원하지 않는 상태인 경우
     */
    public static InquiryStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("문의 상태가 비어있습니다.");
        }

        for (InquiryStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                    status.description.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 문의 상태입니다: " + value);
    }

    /**
     * 상태가 유효한지 확인합니다.
     * @param value 상태 문자열
     * @return 유효 여부
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        for (InquiryStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                    status.description.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated from() 메소드를 사용하세요.
     */
    @Deprecated
    public static InquiryStatus fromString(String value) {
        return from(value);
    }
}