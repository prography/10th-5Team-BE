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
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.RegionGroup;
import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign API", description = "캠페인(체험단) 관련 API")
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * 전체 캠페인 목록 조회 API
     */
    @Operation(summary = "전체 캠페인 목록 조회", description = "전체 캠페인 목록을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getAllCampaigns(
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaigns(null, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("전체 캠페인 목록 조회가 완료되었습니다.", result));
    }

    /**
     * 지역 캠페인 목록 조회 API
     */
    @Operation(summary = "지역 캠페인 목록 조회", description = "지역 캠페인 목록을 조회합니다.")
    @GetMapping("/local")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getLocalCampaigns(
        @Parameter(description = "지역 그룹 (예: seoul, gyeonggi_incheon, jeju 등)", example = "seoul")
        @RequestParam(required = false) List<String> regionGroup,
        @Parameter(description = "하위 지역 (예: gangnam_nonhyeon 등)")
        @RequestParam(required = false) List<String> subRegion,
        @Parameter(description = "로컬 카테고리 (예: restaurant, beauty, accommodation 등)", example = "restaurant")
        @RequestParam(required = false) List<String> localCategory,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaignsByLocal(regionGroup, subRegion, localCategory, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("지역 캠페인 목록 조회가 완료되었습니다.", result));
    }

    /**
     * 제품 캠페인 목록 조회 API
     */
    @Operation(summary = "제품 캠페인 목록 조회", description = "제품 캠페인 목록을 조회합니다.")
    @GetMapping("/product")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getProductCampaigns(
        @Parameter(description = "제품 카테고리 (예: food, beauty, etc)", example = "food")
        @RequestParam(required = false) List<String> productCategory,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaignsByProduct(productCategory, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("제품 캠페인 목록 조회가 완료되었습니다.", result));
    }

    /**
     * 기자단 캠페인 목록 조회 API
     */
    @Operation(summary = "기자단 캠페인 목록 조회", description = "기자단 캠페인 목록을 조회합니다.")
    @GetMapping("/reporter")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getReporterCampaigns(
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignService.getCampaigns(CampaignType.REPORTER, sort, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("기자단 캠페인 목록 조회가 완료되었습니다.", result));
    }

    @Operation(summary = "캠페인 타입별 조회 (Fade out 예정)", description = "캠페인 타입(ALL, PRODUCT, REGION, REPORTER, ETC)별로 캠페인 목록을 조회합니다.")
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
        description = "title에 키워드가 포함된 캠페인(지역/제품 타입)만 검색합니다."
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