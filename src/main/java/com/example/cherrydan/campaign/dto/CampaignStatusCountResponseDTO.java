package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 체험단 상태별 카운트 응답")
public class CampaignStatusCountResponseDTO {
    @Schema(description = "지원한 공고 개수")
    private long apply;
    @Schema(description = "선정 결과 개수")
    private long selected;
    @Schema(description = "미선정 결과 개수")
    private long notSelected;
    @Schema(description = "리뷰 작성 중 개수")
    private long reviewing;
    @Schema(description = "작성 완료 개수")
    private long ended;
}


