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

    public static InquiryStatus fromString(String value) {
        for (InquiryStatus status : InquiryStatus.values()) {
            if (status.name().equalsIgnoreCase(value) ||
                    status.description.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid inquiry status: " + value);
    }
}