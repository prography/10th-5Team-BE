package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    // 체험단 타입별 조회 API (제품, 지역, 기자단, 기타)
    @GetMapping("/types")
    public CampaignListResponseDTO getCampaignsByType(
        @RequestParam(required = false, defaultValue = "all") String type,
        @RequestParam(required = false) String region,
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);

        System.out.println("type: " + type);
        System.out.println("region: " + region);
        System.out.println("sort: " + sort);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        CampaignType campaignType = null;
        if (!"all".equalsIgnoreCase(type.trim())) {
            try {
                String upperCaseType = type.trim().toUpperCase();
                campaignType = CampaignType.valueOf(upperCaseType);
                System.out.println("Client sent: " + type + " → Converted to: " + upperCaseType + " → Enum: " + campaignType);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid campaign type: " + type + ", will fetch all campaigns");
            }
        } else {
            System.out.println("Fetching all campaigns (no type filter)");
        }
        
        return campaignService.getCampaigns(campaignType, region, sort, pageable);
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