package com.example.cherrydan.campaign.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
@Schema(description = "내 체험단 상태 목록 및 카운트 응답 DTO")
public class CampaignStatusListResponseDTO {
    @Schema(description = "신청 상태 리스트")
    private List<CampaignStatusResponseDTO> apply;
    @Schema(description = "선정 상태 리스트")
    private List<CampaignStatusResponseDTO> selected;
    @Schema(description = "등록 상태 리스트")
    private List<CampaignStatusResponseDTO> registered;
    @Schema(description = "종료 상태 리스트")
    private List<CampaignStatusResponseDTO> ended;
    @Schema(description = "상태별 개수 맵", example = "{'apply':4,'selected':2,'registered':1,'ended':3}")
    private Map<String, Long> count;
} 