package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "특정 상태의 내 체험단 팝업용 응답 DTO")
public class CampaignStatusPopupByTypeResponseDTO {
    
    @Schema(description = "총 개수", example = "4", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalCount;

    @Schema(description = "팝업 아이템 리스트 (최대 4개)", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CampaignStatusPopupItemDTO> items;
}
