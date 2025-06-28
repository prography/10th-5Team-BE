package com.example.cherrydan.campaign.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
@Schema(description = "내 체험단 팝업용 상태 응답 DTO")
public class CampaignStatusPopupResponseDTO {
    @Schema(description = "신청 상태 총 개수", example = "4")
    private long applyTotal;
    @Schema(description = "선정 상태 총 개수", example = "2")
    private long selectedTotal;
    @Schema(description = "등록 상태 총 개수", example = "1")
    private long registeredTotal;
    @Schema(description = "신청 상태 리스트")
    private List<CampaignStatusResponseDTO> apply;
    @Schema(description = "선정 상태 리스트")
    private List<CampaignStatusResponseDTO> selected;
    @Schema(description = "등록 상태 리스트")
    private List<CampaignStatusResponseDTO> registered;
}