package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class CampaignStatusRequestDTO {
    private Long campaignId;
    @JsonIgnore
    private Long userId;
    @Schema(description = "캠페인 상태 타입 (APPLY: 신청, SELECTED: 선정, REGISTERED: 등록, ENDED: 종료)", example = "APPLY", allowableValues = {"APPLY", "SELECTED", "REGISTERED", "ENDED"})
    private CampaignStatusType status;
    private Boolean isActive;
} 