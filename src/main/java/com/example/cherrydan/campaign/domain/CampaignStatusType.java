package com.example.cherrydan.campaign.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "캠페인 상태 타입 (APPLY: 지원한 공고, SELECTED: 선정 결과, NOT_SELECTED: 미선정 결과, REVIEWING: 리뷰 작성 중, ENDED: 작성 완료)")
@Getter
public enum CampaignStatusType {
    APPLY("apply", "지원한 공고"),
    SELECTED("selected", "선정 결과"),
    NOT_SELECTED("not_selected", "미선정 결과"),
    REVIEWING("reviewing", "리뷰 작성 중"),
    ENDED("ended", "작성 완료");

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