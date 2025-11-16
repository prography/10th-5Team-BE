package com.example.cherrydan.campaign.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "캠페인 상태 케이스 타입")
@Getter
public enum CampaignStatusCase {
    APPLIED_COMPLETED("appliedCompleted", "결과 발표 완료"),
    APPLIED_WAITING("appliedWaiting", "발표 기다리는중"),
    RESULT_SELECTED("resultSelected", "선정된 공고"),
    RESULT_NOT_SELECTED("resultNotSelected", "선정되지 않은 공고"),
    REVIEW_IN_PROGRESS("reviewInProgress", "리뷰 작성할 공고"),
    REVIEW_COMPLETED("reviewCompleted", "리뷰 작성 완료한 공고");

    private final String code;
    private final String label;

    CampaignStatusCase(String code, String label) {
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
     * 코드로 CampaignStatusCase 찾기
     */
    public static CampaignStatusCase fromCode(String code) {
        for (CampaignStatusCase statusCase : values()) {
            if (statusCase.code.equalsIgnoreCase(code)) {
                return statusCase;
            }
        }
        throw new IllegalArgumentException("Invalid CampaignStatusCase code: " + code);
    }

    /**
     * CampaignStatusCase를 CampaignStatusType으로 변환
     */
    public CampaignStatusType toStatusType() {
        return switch (this) {
            case APPLIED_WAITING, APPLIED_COMPLETED -> CampaignStatusType.APPLY;
            case RESULT_SELECTED -> CampaignStatusType.SELECTED;
            case RESULT_NOT_SELECTED -> CampaignStatusType.NOT_SELECTED;
            case REVIEW_IN_PROGRESS -> CampaignStatusType.REVIEWING;
            case REVIEW_COMPLETED -> CampaignStatusType.ENDED;
        };
    }
}

