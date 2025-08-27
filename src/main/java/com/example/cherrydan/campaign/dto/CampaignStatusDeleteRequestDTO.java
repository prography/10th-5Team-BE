package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캠페인 상태 배치 삭제 요청")
public class CampaignStatusDeleteRequestDTO {
    @Schema(description = "삭제할 캠페인 ID 목록", example = "[1, 2, 3]", required = true)
    private List<Long> campaignIds;
}