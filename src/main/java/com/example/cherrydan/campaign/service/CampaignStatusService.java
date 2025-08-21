package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusCountResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CampaignStatusService {
    CampaignStatusResponseDTO createOrRecoverStatus(CampaignStatusRequestDTO requestDTO);
    CampaignStatusResponseDTO updateStatus(CampaignStatusRequestDTO requestDTO);
    void deleteStatus(Long campaignId, Long userId);
    CampaignStatusPopupResponseDTO getPopupStatusByUser(Long userId);
    PageListResponseDTO<CampaignStatusResponseDTO> getStatusesByType(Long userId, CampaignStatusType statusType, String subFilter, Pageable pageable);
    CampaignStatusCountResponseDTO getStatusCounts(Long userId);
} 