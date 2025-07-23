package com.example.cherrydan.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    ACTIVITY_REMINDER("activity_reminder", "활동 리마인더"),
    KEYWORD_CAMPAIGN("keyword_campaign", "키워드 맞춤 캠페인"),
    GENERAL("general", "일반 알림");

    private final String code;
    private final String label;

    NotificationType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown notification type: " + code);
    }
} 