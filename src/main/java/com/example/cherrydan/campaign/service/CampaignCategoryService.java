package com.example.cherrydan.campaign.service;

import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CampaignCategoryService {
    PageListResponseDTO<CampaignResponseDTO> searchByCategory(
        String title,
        List<String> regionGroup,
        List<String> subRegion,
        List<String> local,
        List<String> product,
        String reporter,
        List<String> snsPlatform,
        List<String> campaignPlatform,
        String applyStart,
        String applyEnd,
        Pageable pageable
    );
} 