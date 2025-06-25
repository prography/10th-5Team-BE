package com.example.cherrydan.campaign.domain;

import lombok.Getter;

@Getter
public enum SnsPlatformType {
    ALL("all", "전체"),
    BLOG("blog", "블로그"),
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
        for (SnsPlatformType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        throw new IllegalArgumentException("Unknown platform code: " + code);
    }
} 