package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.*;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CampaignStatusService {
    CampaignStatusResponseDTO createOrRecoverStatus(CampaignStatusRequestDTO requestDTO, Long userId);
    List<CampaignStatusResponseDTO> updateStatusBatch(CampaignStatusBatchRequestDTO requestDTO, Long userId);
    void deleteStatusBatch(CampaignStatusDeleteRequestDTO requestDTO, Long userId);
    CampaignStatusPopupByTypeResponseDTO getPopupStatusByType(Long userId, CampaignStatusType statusType);
    PageListResponseDTO<CampaignStatusResponseDTO> getStatusesByType(Long userId, CampaignStatusType statusType, Pageable pageable);
    CampaignStatusCountResponseDTO getStatusCounts(Long userId);
} 