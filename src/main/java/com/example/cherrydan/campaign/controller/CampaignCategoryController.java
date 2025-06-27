package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns/categories")
@RequiredArgsConstructor
@Tag(name = "Campaign Category API", description = "카테고리별 캠페인 검색 API")
public class CampaignCategoryController {

    private final CampaignCategoryService campaignCategoryService;

    /**
     * 카테고리별 캠페인 검색 API
     * ex) /api/campaigns/categories/search?regionGroup=서울&product=식품
     */
    @Operation(
        summary = "카테고리별 캠페인 검색",
        description = "여러 카테고리(지역, 제품, 기자단, SNS, 체험단 플랫폼 등) 조건으로 캠페인 목록을 검색합니다.\n\n"
            + "regionGroup, subRegion, local, product, reporter, snsPlatform, experiencePlatform 중 원하는 조건만 조합해서 검색할 수 있습니다.\n"
            + "정렬(sort): popular(인기순), latest(최신순), deadline(마감임박순), low_competition(경쟁률 낮은순)"
    )
    @GetMapping("/search")
    public CampaignListResponseDTO searchByCategory(
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
        @Parameter(description = "체험단 플랫폼 (예: chvu, revu 등) - 복수 선택 가능")
        @RequestParam(required = false) List<String> experiencePlatform,
        @Parameter(description = "마감일 시작일 (예: 2025-06-01)", example = "2025-06-01")
        @RequestParam(required = false) String applyStart,
        @Parameter(description = "마감일 종료일 (예: 2025-06-30)", example = "2025-06-30")
        @RequestParam(required = false) String applyEnd,
        @Parameter(description = "정렬 기준 (popular, latest, deadline, low_competition)", example = "popular")
        @RequestParam(required = false, defaultValue = "popular") String sort,
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(required = false, defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(sort, page, size);

        return campaignCategoryService.searchByCategory(regionGroup, subRegion, local, product, reporter, snsPlatform, experiencePlatform, applyStart, applyEnd, pageable);
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