package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CampaignCategoryService {
    CampaignListResponseDTO searchByCategory(
        String regionGroup,
        String subRegion,
        String local,
        String product,
        String reporter,
        String snsPlatform,
        String experiencePlatform,
        Pageable pageable
    );
} 