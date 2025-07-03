package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
    PageListResponseDTO<CampaignResponseDTO> getCampaigns(
        CampaignType type,
        String sort,
        Pageable pageable
    );
    
    PageListResponseDTO<CampaignResponseDTO> getCampaignsBySnsPlatform(
        SnsPlatformType snsPlatformType,
        String sort,
        Pageable pageable
    );
    
    PageListResponseDTO<CampaignResponseDTO> getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable);

    PageListResponseDTO<CampaignResponseDTO> searchByKeyword(String keyword, Pageable pageable);
} 