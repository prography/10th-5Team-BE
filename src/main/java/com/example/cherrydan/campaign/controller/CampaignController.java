package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign API", description = "캠페인(체험단) 관련 API")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping("/types")
    public CampaignListResponseDTO getCampaignsByType(
        @RequestParam(required = false, defaultValue = "all") String type,
        @RequestParam(required = false) String region,
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);
        CampaignType campaignType = null;
        if (!"all".equalsIgnoreCase(type.trim())) {
            try {
                String upperCaseType = type.trim().toUpperCase();
                campaignType = CampaignType.valueOf(upperCaseType);
            } catch (IllegalArgumentException e) {
                campaignType = CampaignType.ALL;
            }
        }
        return campaignService.getCampaigns(campaignType, region, sort, pageable);
    }

    @GetMapping("/campaign-platforms")
    public CampaignListResponseDTO getCampaignsByCampaignPlatform(
        @RequestParam(required = false, defaultValue = "all") String platform,
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);
        CampaignPlatformType platformType = CampaignPlatformType.ALL;
        if (!"all".equalsIgnoreCase(platform.trim())) {
            try {
                platformType = CampaignPlatformType.fromCode(platform.trim());
            } catch (IllegalArgumentException e) {
                platformType = CampaignPlatformType.ALL;
            }
        }
        return campaignService.getCampaignsByCampaignPlatform(platformType, sort, pageable);
    }

    @GetMapping("/sns-platforms")
    public CampaignListResponseDTO getCampaignsBySnsPlatform(
        @RequestParam(required = false, defaultValue = "all") String platform,
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);
        SnsPlatformType snsPlatformType = SnsPlatformType.ALL;
        if (!"all".equalsIgnoreCase(platform.trim())) {
            try {
                snsPlatformType = SnsPlatformType.fromCode(platform.trim());
            } catch (IllegalArgumentException e) {
                snsPlatformType = SnsPlatformType.ALL;
            }
        }
        return campaignService.getCampaignsBySnsPlatform(snsPlatformType, sort, pageable);
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