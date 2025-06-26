package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns/categories")
@RequiredArgsConstructor
public class CampaignCategoryController {

    private final CampaignCategoryService campaignCategoryService;

    /**
     * 카테고리별 캠페인 검색 API
     * ex) /api/campaigns/categories/search?regionGroup=서울&product=식품
     */
    @GetMapping("/search")
    public CampaignListResponseDTO searchByCategory(
        @RequestParam(required = false) String regionGroup,
        @RequestParam(required = false) String subRegion,
        @RequestParam(required = false) String local,
        @RequestParam(required = false) String product,
        @RequestParam(required = false) String reporter,
        @RequestParam(required = false) String snsPlatform,
        @RequestParam(required = false) String experiencePlatform,
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);

        return campaignCategoryService.searchByCategory(regionGroup, subRegion, local, product, reporter, snsPlatform, experiencePlatform, pageable);
    }

    private Pageable createPageable(String sort, int page, int size) {
        switch (sort) {
            case "popular":
                return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "competitionRate"));
            case "latest":
                return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyStart"));
            case "deadline":
                return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "applyEnd"));
            case "low_competition":
                return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "competitionRate"));
            default:
                return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "competitionRate"));
        }
    }
} 