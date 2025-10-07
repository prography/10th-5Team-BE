package com.example.cherrydan.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.cherrydan.campaign.domain.CampaignSite;
import com.example.cherrydan.common.util.CloudfrontUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignSiteResponseDTO {

    @Deprecated
    @Schema(description = "기존 플랫폼 한글명(deprecated)", deprecated = true, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String siteNameKr;

    @Deprecated
    @Schema(description = "기존 플랫폼 영문명(deprecated)", deprecated = true, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String siteNameEn;

    @Deprecated
    @Schema(description = "기존 플랫폼 CDN URL(deprecated)", deprecated = true, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cdnUrl;

    @Schema(description = "캠페인 플랫폼 이미지 URL", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteUrl;

    @Schema(description = "캠페인 플랫폼 한글명", example = "레뷰", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteKr;

    @Schema(description = "캠페인 플랫폼 영문 코드", example = "revu", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteEn;
} 