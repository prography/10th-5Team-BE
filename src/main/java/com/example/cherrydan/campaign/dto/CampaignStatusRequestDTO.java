package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatusType;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
public class CampaignStatusRequestDTO {
    private Long campaignId;
    @JsonIgnore
    private Long userId;
    private CampaignStatusType status;
    private Boolean isActive;
} 