package com.example.cherrydan.campaign.domain;

import lombok.Getter;

@Getter
public enum SnsPlatformType {
    ALL("all", "전체"),
    BLOG("blog", "네이버 블로그"),
    INSTAGRAM("insta", "인스타그램"),
    YOUTUBE("youtube", "유튜브"),
    TIKTOK("tiktok", "틱톡"),
    ETC("etc", "기타");

    private final String code;
    private final String label;

    SnsPlatformType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SnsPlatformType fromCode(String code) {
        String trimmedCode = code.trim();
        for (SnsPlatformType type : values()) {
            if (type.code.equalsIgnoreCase(trimmedCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown platform code: " + code);
    }

    public String[] getRelatedFields() {
        switch (this) {
            case BLOG:
                return new String[]{"blog", "clip"};
            case YOUTUBE:
                return new String[]{"youtube", "shorts"};
            case INSTAGRAM:
                return new String[]{"insta", "reels"};
            case TIKTOK:
                return new String[]{"tiktok"};
            case ETC:
                return new String[]{"etc"};
            default:
                return new String[]{};
        }
    }
} 