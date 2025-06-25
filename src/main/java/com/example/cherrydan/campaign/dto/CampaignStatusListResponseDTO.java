package com.example.cherrydan.campaign.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CampaignStatusListResponseDTO {
    private List<CampaignStatusResponseDTO> apply;
    private List<CampaignStatusResponseDTO> selected;
    private List<CampaignStatusResponseDTO> registered;
    private List<CampaignStatusResponseDTO> ended;
    private Map<String, Long> count;
} 