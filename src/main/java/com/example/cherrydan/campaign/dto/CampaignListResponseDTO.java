package com.example.cherrydan.campaign.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class CampaignListResponseDTO {
    private List<CampaignResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
} 