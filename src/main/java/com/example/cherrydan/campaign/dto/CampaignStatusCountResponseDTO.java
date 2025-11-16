package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 체험단 상태별 카운트 응답")
public class CampaignStatusCountResponseDTO {
    @Schema(description = "결과 발표 완료 개수")
    private long appliedCompleted;
    @Schema(description = "발표 기다리는 중 개수")
    private long appliedWaiting;
    @Schema(description = "선정된 공고 개수")
    private long resultSelected;
    @Schema(description = "선정되지 않은 공고 개수")
    private long resultNotSelected;
    @Schema(description = "리뷰 작성할 공고 개수")
    private long reviewInProgress;
    @Schema(description = "리뷰 작성 완료한 공고 개수")
    private long reviewCompleted;
}


