package com.example.cherrydan.campaign.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "북마크 케이스 타입")
@Getter
public enum BookmarkCase {
    LIKED_OPEN("likedOpen", "신청 가능한 공고"),
    LIKED_CLOSED("likedClosed", "신청 마감된 공고");

    private final String code;
    private final String label;

    BookmarkCase(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 코드로 BookmarkCase 찾기
     */
    public static BookmarkCase fromCode(String code) {
        for (BookmarkCase bookmarkCase : values()) {
            if (bookmarkCase.code.equalsIgnoreCase(code)) {
                return bookmarkCase;
            }
        }
        throw new IllegalArgumentException("Invalid BookmarkCase code: " + code);
    }
}

