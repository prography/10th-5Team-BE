package com.example.cherrydan.campaign.domain;

import lombok.Getter;

@Getter
public enum CampaignType {
    REGION(1, "지역"),
    PRODUCT(2, "제품"),
    REPORTER(3, "기자단"),
    ETC(4, "기타");

    private final int code;
    private final String label;

    CampaignType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CampaignType fromCode(int code) {
        for (CampaignType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
} 