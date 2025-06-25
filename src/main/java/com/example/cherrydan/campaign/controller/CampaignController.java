package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "캠페인 타입별 조회", description = "캠페인 타입(ALL, PRODUCT, REGION, REPORTER, ETC)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/types")
    public CampaignListResponseDTO getCampaignsByType(
        @Parameter(description = "캠페인 타입 (all, product, region, reporter, etc)", example = "all")
        @RequestParam(required = false, defaultValue = "all") String type,
        @Parameter(description = "지역명 (선택)")
        @RequestParam(required = false) String region,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);
        CampaignType campaignType = null;
        try {
            String upperCaseType = type.trim().toUpperCase();
            campaignType = CampaignType.valueOf(upperCaseType);
        } catch (IllegalArgumentException e) {
            campaignType = null;
        }
        return campaignService.getCampaigns(campaignType, region, sort, pageable);
    }

    @Operation(summary = "캠페인 플랫폼별 조회", description = "캠페인 플랫폼(cherivu, revu, reviewnote 등)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/campaign-platforms")
    public CampaignListResponseDTO getCampaignsByCampaignPlatform(
        @Parameter(description = "캠페인 플랫폼 코드 (all, chvu, revu, reviewnote, dailyview, fourblog, popomon, dinnerqueen, seoulouba, cometoplay, gangnam)", example = "fourblog")
        @RequestParam(required = false, defaultValue = "all") String platform,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
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

    @Operation(summary = "SNS 플랫폼별 조회", description = "SNS 플랫폼(blog, insta, youtube, tiktok, etc)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/sns-platforms")
    public CampaignListResponseDTO getCampaignsBySnsPlatform(
        @Parameter(description = "SNS 플랫폼 코드 (all, blog, insta, youtube, tiktok, etc)", example = "blog")
        @RequestParam(required = false, defaultValue = "all") String platform,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
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