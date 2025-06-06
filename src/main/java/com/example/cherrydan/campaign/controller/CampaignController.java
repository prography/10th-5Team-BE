package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignFilterDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.dto.RegionSearchDTO;
import com.example.cherrydan.campaign.service.CampaignService;
import com.example.cherrydan.common.dto.PageResponseDTO;
import com.example.cherrydan.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 캠페인 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "캠페인", description = "캠페인 검색 API")
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * 1. 지역 + 카테고리 검색
     */
    @PostMapping("/search/region")
    @Operation(summary = "지역별 캠페인 검색", 
               description = "특정 지역과 카테고리로 캠페인을 검색합니다.")
    public ResponseEntity<ApiResponse<PageResponseDTO<CampaignResponseDTO>>> searchByRegion(
            @RequestBody RegionSearchDTO searchDto,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        log.info("지역별 캠페인 검색: {}", searchDto);

        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<CampaignResponseDTO> campaigns = campaignService.searchByRegionAndCategory(searchDto, pageable);

        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    /**
     * 2. 키워드 + 필터 검색
     */
    @PostMapping("/search/filter")
    @Operation(summary = "키워드 + 필터 검색", 
               description = "키워드와 다양한 필터 조건으로 캠페인을 검색합니다.")
    public ResponseEntity<ApiResponse<PageResponseDTO<CampaignResponseDTO>>> searchWithFilters(
            @RequestBody CampaignFilterDTO filter,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        log.info("키워드+필터 검색: {}", filter);

        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<CampaignResponseDTO> campaigns = campaignService.searchWithFilters(filter, pageable);

        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    /**
     * 캠페인 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "캠페인 상세 조회", description = "특정 캠페인의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CampaignResponseDTO>> getCampaign(
            @Parameter(description = "캠페인 ID") @PathVariable Long id) {

        log.info("캠페인 상세 조회: id={}", id);

        CampaignResponseDTO campaign = campaignService.getCampaign(id);
        return ResponseEntity.ok(ApiResponse.success(campaign));
    }
}