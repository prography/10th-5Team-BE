package com.example.cherrydan.campaign.domain;

import lombok.Getter;

@Getter
public enum SnsPlatformType {
    ALL("all", "전체"),
    BLOG("blog", "네이버 블로그"),
    CLIP("clip", "네이버 클립"),
    INSTAGRAM("insta", "인스타그램"),
    REELS("reels", "인스타그램 릴스"),
    YOUTUBE("youtube", "유튜브"),
    SHORTS("shorts", "유튜브 쇼츠"),
    TIKTOK("tiktok", "틱톡"),
    THREAD("thread", "스레드"),
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
                return new String[]{"blog"};
            case CLIP:
                return new String[]{"clip"};
            case YOUTUBE:
                return new String[]{"youtube"};
            case SHORTS:
                return new String[]{"shorts"};
            case INSTAGRAM:
                return new String[]{"insta"};
            case REELS:
                return new String[]{"reels"};
            case TIKTOK:
                return new String[]{"tiktok"};
            case THREAD:
                return new String[]{"thread"};
            case ETC:
                return new String[]{"etc"};
            default:
                return new String[]{};
        }
    }
} 