package com.example.cherrydan.campaign.domain;

import lombok.Getter;

@Getter
public enum CampaignPlatformType {
    ALL("all", "전체"),
    CHVU("chvu", "체험뷰"),
    REVU("revu", "레뷰"),
    REVIEWNOTE("reviewnote", "리뷰노트"),
    DAILYVIEW("dailyview", "데일리뷰"),
    FOURBLOG("4blog", "포블로그"),
    POPOMON("popomon", "포포몬"),
    DINNERQUEEN("dinnerqueen", "디너의여왕"),
    SEOULOUBA("seoulouba", "서울오빠"),
    COMETOPLAY("cometoplay", "놀러와체험단"),
    GANGNAM("gangnam", "강남맛집");

    private final String code;
    private final String label;

    CampaignPlatformType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CampaignPlatformType fromCode(String code) {
        String trimmedCode = code.trim();
        for (CampaignPlatformType type : values()) {
            if (type.code.equalsIgnoreCase(trimmedCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CampaignPlatformType code: " + code);
    }
} 