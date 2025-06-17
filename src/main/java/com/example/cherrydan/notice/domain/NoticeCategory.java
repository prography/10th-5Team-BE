package com.example.cherrydan.notice.domain;

import lombok.Getter;

@Getter
public enum NoticeCategory {
    NOTICE("공지사항"),
    TIP("팁"),
    EVENT("이벤트");

    private final String description;

    NoticeCategory(String description) {
        this.description = description;
    }

    public static NoticeCategory fromString(String value) {
        for (NoticeCategory category : NoticeCategory.values()) {
            if (category.name().equalsIgnoreCase(value) ||
                    category.description.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid notice category: " + value);
    }
}