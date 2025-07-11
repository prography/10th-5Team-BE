package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.service.CampaignCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;

@RestController
@RequestMapping("/api/campaigns/categories")
@RequiredArgsConstructor
@Tag(name = "Campaign API", description = "캠페인(체험단) 관련 API")
public class CampaignCategoryController {

    private final CampaignCategoryService campaignCategoryService;

    /**
     * 카테고리별 캠페인 검색 API
     * ex) /api/campaigns/categories/search?regionGroup=서울&product=식품
     */
    @Operation(
        summary = "카테고리별 캠페인 검색",
        description = "여러 카테고리(제목, 지역, 제품, 기자단, SNS, 캠페인 플랫폼 등) 조건으로 캠페인 목록을 검색합니다.\n\n"
            + "title, regionGroup, subRegion, local, product, reporter, snsPlatform, campaignPlatform 중 원하는 조건만 조합해서 검색할 수 있습니다.\n"
            + "정렬(sort): popular(인기순), latest(최신순), deadline(마감임박순), low_competition(경쟁률 낮은순)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> searchByCategory(
        @Parameter(description = "제목 (예: 캠페인 제목)")
        @RequestParam(required = false) String title,
        @Parameter(description = "지역 그룹 (예: seoul, gyeonggi_incheon 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> regionGroup,
        @Parameter(description = "하위 지역 (예: gangnam_nonhyeon 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> subRegion,
        @Parameter(description = "로컬 카테고리 (예: restaurant, beauty 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> local,
        @Parameter(description = "제품 카테고리 (예: food, beauty 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> product,
        @Parameter(description = "기자단 여부 (예: reporter)")
        @RequestParam(required = false) String reporter,
        @Parameter(description = "SNS 플랫폼 (예: blog, youtube, insta, tiktok, etc) - 복수 선택 가능")
        @RequestParam(required = false) List<String> snsPlatform,
        @Parameter(description = "캠페인 플랫폼 (예: chvu, revu 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> campaignPlatform,
        @Parameter(description = "마감일 시작일 (예: 2025-06-01)", example = "2025-06-01")
        @RequestParam(required = false) String applyStart,
        @Parameter(description = "마감일 종료일 (예: 2025-06-30)", example = "2025-06-30")
        @RequestParam(required = false) String applyEnd,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = createPageable(sort, page, size);
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        PageListResponseDTO<CampaignResponseDTO> result = campaignCategoryService.searchByCategory(title, regionGroup, subRegion, local, product, reporter, snsPlatform, campaignPlatform, applyStart, applyEnd, pageable, userId);
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