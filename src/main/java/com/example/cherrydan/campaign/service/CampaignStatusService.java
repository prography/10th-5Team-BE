package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;

public interface CampaignStatusService {
    CampaignStatusResponseDTO createOrRecoverStatus(CampaignStatusRequestDTO requestDTO);
    CampaignStatusListResponseDTO getStatusListWithCountByUser(Long userId);
    CampaignStatusResponseDTO updateStatus(CampaignStatusRequestDTO requestDTO);
    void deleteStatus(Long campaignId, Long userId);
    CampaignStatusPopupResponseDTO getPopupStatusByUser(Long userId);
} 