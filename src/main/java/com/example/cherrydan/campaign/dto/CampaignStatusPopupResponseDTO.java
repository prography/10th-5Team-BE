package com.example.cherrydan.campaign.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CampaignStatusPopupResponseDTO {
    private long applyTotal;
    private long selectedTotal;
    private long registeredTotal;
    private List<CampaignStatusResponseDTO> apply;
    private List<CampaignStatusResponseDTO> selected;
    private List<CampaignStatusResponseDTO> registered;
} 