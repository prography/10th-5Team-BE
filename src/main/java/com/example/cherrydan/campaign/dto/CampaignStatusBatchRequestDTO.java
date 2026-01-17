package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캠페인 상태 배치 업데이트 요청")
public class CampaignStatusBatchRequestDTO {

    @NotEmpty(message = "캠페인 ID 목록은 필수입니다.")
    @Schema(description = "업데이트할 캠페인 ID 목록", example = "[1, 2, 3]", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> campaignIds;

    @Schema(description = "변경할 캠페인 상태", example = "SELECTED", allowableValues = {"APPLY", "SELECTED", "NOT_SELECTED", "REVIEWING", "ENDED"}, nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private CampaignStatusType status;

    @Schema(description = "활성 상태 여부", example = "true", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;
}