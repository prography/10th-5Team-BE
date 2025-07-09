package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign API", description = "캠페인(체험단) 관련 API")
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "캠페인 타입별 조회", description = "캠페인 타입(ALL, PRODUCT, REGION, REPORTER, ETC)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getCampaignsByType(
        @Parameter(description = "캠페인 타입 (all, product, region, reporter, etc)", example = "all")
        @RequestParam(required = false, defaultValue = "all") String type,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        CampaignType campaignType = null;
        try {
            String upperCaseType = type.trim().toUpperCase();
            campaignType = CampaignType.valueOf(upperCaseType);
        } catch (IllegalArgumentException e) {
            campaignType = null;
        }
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaigns(campaignType, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("캠페인 목록 조회가 완료되었습니다.", result));
    }

    @Operation(summary = "캠페인 플랫폼별 조회", description = "캠페인 플랫폼(cherivu, revu, reviewnote 등)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/campaign-platforms")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getCampaignsByCampaignPlatform(
        @Parameter(description = "캠페인 플랫폼 코드 (all, chvu, revu, reviewnote, dailyview, fourblog, popomon, dinnerqueen, seoulouba, cometoplay, gangnam)", example = "fourblog")
        @RequestParam(required = false, defaultValue = "all") String platform,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
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
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaignsByCampaignPlatform(platformType, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("캠페인 목록 조회가 완료되었습니다.", result));
    }

    @Operation(summary = "SNS 플랫폼별 조회", description = "SNS 플랫폼(blog, insta, youtube, tiktok, etc)별로 캠페인 목록을 조회합니다.")
    @GetMapping("/sns-platforms")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getCampaignsBySnsPlatform(
        @Parameter(description = "SNS 플랫폼 코드 (all, blog, insta, youtube, tiktok, etc)", example = "blog")
        @RequestParam(required = false, defaultValue = "all") String platform,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
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
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaignsBySnsPlatform(snsPlatformType, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("캠페인 목록 조회가 완료되었습니다.", result));
    }

    @Operation(
        summary = "키워드 기반 캠페인 검색",
        description = "title에 키워드가 포함된 캠페인(지역/제품 타입, is_active=1)만 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> searchCampaignsByKeyword(
        @Parameter(description = "검색 키워드 (title에 포함)")
        @RequestParam String keyword,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.searchByKeyword(keyword, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("캠페인 목록 조회가 완료되었습니다.", result));
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