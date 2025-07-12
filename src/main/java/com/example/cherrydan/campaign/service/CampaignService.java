package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
    PageListResponseDTO<CampaignResponseDTO> getCampaigns(
        CampaignType type,
        String sort,
        Pageable pageable,
        Long userId
    );
    
    PageListResponseDTO<CampaignResponseDTO> getCampaignsBySnsPlatform(
        SnsPlatformType snsPlatformType,
        String sort,
        Pageable pageable,
        Long userId
    );
    
    PageListResponseDTO<CampaignResponseDTO> getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable, Long userId);

    PageListResponseDTO<CampaignResponseDTO> searchByKeyword(String keyword, Pageable pageable, Long userId);

    Page<CampaignResponseDTO> getPersonalizedCampaignsByKeyword(Long userId, String keyword, Pageable pageable);

    long getCampaignCountByKeyword(String keyword);
} 