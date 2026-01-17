package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CampaignStatusRequestDTO {
    @NotNull(message = "캠페인 ID는 필수입니다.")
    @Schema(description = "캠페인 ID", example = "123", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long campaignId;

    @NotNull(message = "상태는 필수입니다.")
    @Schema(description = "캠페인 상태 타입 (APPLY: 지원한 공고, SELECTED: 선정 결과, NOT_SELECTED: 미선정 결과, REVIEWING: 리뷰 작성 중, ENDED: 작성 완료)", example = "APPLY", allowableValues = {"APPLY", "SELECTED", "NOT_SELECTED", "REVIEWING", "ENDED"}, nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private CampaignStatusType status;

    @Schema(description = "활성 상태 여부", example = "true", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;
} 