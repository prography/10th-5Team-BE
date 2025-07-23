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

    /**
     * 문자열로부터 NoticeCategory를 반환합니다.
     * @param value 카테고리 문자열 (대소문자 무관)
     * @return 해당하는 NoticeCategory
     * @throws IllegalArgumentException 지원하지 않는 카테고리인 경우
     */
    public static NoticeCategory from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("공지 카테고리가 비어있습니다.");
        }

        for (NoticeCategory category : values()) {
            if (category.name().equalsIgnoreCase(value.trim()) ||
                    category.description.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 공지 카테고리입니다: " + value);
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

        for (NoticeCategory category : values()) {
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
    public static NoticeCategory fromString(String value) {
        return from(value);
    }
}