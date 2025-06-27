package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CampaignCategoryService {
    CampaignListResponseDTO searchByCategory(
        List<String> regionGroup,
        List<String> subRegion,
        List<String> local,
        List<String> product,
        String reporter,
        List<String> snsPlatform,
        List<String> experiencePlatform,
        String applyStart,
        String applyEnd,
        Pageable pageable
    );
} 