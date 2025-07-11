package com.example.cherrydan.campaign.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "캠페인 상태 타입 (APPLY: 신청, SELECTED: 선정, NOT_SELECTED: 미선정, REGISTERED: 등록, ENDED: 종료)")
@Getter
public enum CampaignStatusType {
    APPLY("apply", "신청"),
    SELECTED("selected", "선정"),
    NOT_SELECTED("not_selected", "미선정"),
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