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
    @Schema(description = "기존 플랫폼 한글명(deprecated(v1.0.2까지))", deprecated = true)
    private String siteNameKr;

    @Deprecated
    @Schema(description = "기존 플랫폼 영문명(deprecated(v1.0.2까지))", deprecated = true)
    private String siteNameEn;

    @Deprecated
    @Schema(description = "기존 플랫폼 CDN URL(deprecated(v1.0.2까지))", deprecated = true)
    private String cdnUrl;

    private String campaignSiteUrl;
    private String campaignSiteKr;
    private String campaignSiteEn;
} 