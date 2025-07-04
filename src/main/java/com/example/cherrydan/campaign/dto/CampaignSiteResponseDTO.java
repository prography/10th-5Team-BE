package com.example.cherrydan.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignSiteResponseDTO {
    private String siteNameKr;
    private String cdnUrl;
} 