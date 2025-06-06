package com.example.cherrydan.campaign.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 캠페인 타입 enum
 */
@Getter
@RequiredArgsConstructor
public enum CampaignType {
    REGIONAL(0, "지역(방문)"),
    DELIVERY(1, "제품(배송형)"),
    REPORTER(2, "기자단");

    private final int code;
    private final String description;

    /**
     * 코드로 CampaignType 찾기
     */
    public static CampaignType fromCode(int code) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid campaign type code: " + code));
    }
}
