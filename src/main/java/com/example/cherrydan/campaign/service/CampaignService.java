package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
    CampaignListResponseDTO getCampaigns(
        CampaignType type,
        String region,
        String sort,
        Pageable pageable
    );
    
    CampaignListResponseDTO getCampaignsBySnsPlatform(
        SnsPlatformType snsPlatformType,
        String sort,
        Pageable pageable
    );
    
    CampaignListResponseDTO getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable);

    CampaignListResponseDTO searchByKeyword(String keyword, Pageable pageable);
} 