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

    public static InquiryCategory fromString(String value) {
        for (InquiryCategory category : InquiryCategory.values()) {
            if (category.name().equalsIgnoreCase(value) ||
                    category.description.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid inquiry category: " + value);
    }
}