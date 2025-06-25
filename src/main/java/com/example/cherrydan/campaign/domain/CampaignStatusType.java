package com.example.cherrydan.campaign.domain;

public enum CampaignStatusType {
    APPLY("apply", "신청"),
    SELECTED("selected", "선정"),
    REGISTERED("registered", "등록"),
    ENDED("ended", "종료");

    private final String code;
    private final String label;

    CampaignStatusType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
} 