package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.Region;
import com.example.cherrydan.campaign.domain.RegionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

/**
 * 캠페인 필터링 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캠페인 필터링 조건")
public class CampaignFilterDTO {

    @Schema(description = "키워드 검색", example = "맛집")
    private String keyword;

    // 지역 필터
    @Schema(description = "지역", 
            example = "SEOUL",
            allowableValues = {"ALL", "SEOUL", "SEOUL_GANGNAM_NONHYEON", "SEOUL_GANGDONG_CHEONHO", 
                              "BUSAN", "BUSAN_HAEUNDAE_CENTUM", "DAEGU", "INCHEON", "GWANGJU", 
                              "DAEJEON", "ULSAN", "SEJONG", "GYEONGGI", "GANGWON", "CHUNGBUK", 
                              "CHUNGNAM", "JEONBUK", "JEONNAM", "GYEONGBUK", "GYEONGNAM", "JEJU"})
    private Region region;

    @Schema(description = "지역 카테고리", 
            example = "RESTAURANT",
            allowableValues = {"ALL", "RESTAURANT", "BEAUTY", "ACCOMMODATION", "CULTURE", "DELIVERY", "PACKAGING", "ETC"})
    private RegionCategory regionCategory;

    // 체크박스 필터들 (1 or 0)
    @Schema(description = "제품 캠페인 포함 여부", example = "1")
    private Integer product;

    @Schema(description = "기자단 캠페인 포함 여부", example = "1")
    private Integer reporter;

    @Schema(description = "SNS 플랫폼 필터")
    private SocialPlatformDTO sns;

    @Schema(description = "체험단 플랫폼 포함 여부", example = "1")
    private Integer reviewPlatform;

    // 마감일 필터
    @Schema(description = "마감일 시작", example = "2025-06-01")
    private LocalDate deadlineStart;

    @Schema(description = "마감일 종료", example = "2025-06-30")
    private LocalDate deadlineEnd;

    /**
     * 키워드 검색이 있는지 확인
     */
    public boolean hasKeywordFilter() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 체크박스 필터가 있는지 확인
     */
    public boolean hasCheckboxFilters() {
        return region != null || regionCategory != null || 
               isChecked(product) || isChecked(reporter) || isChecked(reviewPlatform) || 
               (sns != null && sns.hasAnyFilter());
    }

    /**
     * 마감일 필터가 있는지 확인
     */
    public boolean hasDeadlineFilter() {
        return deadlineStart != null || deadlineEnd != null;
    }

    /**
     * 빈 필터인지 확인
     */
    public boolean isEmpty() {
        return !hasKeywordFilter() && !hasCheckboxFilters() && !hasDeadlineFilter();
    }

    /**
     * 체크박스 활성화 여부 확인
     */
    private boolean isChecked(Integer value) {
        return Integer.valueOf(1).equals(value);
    }
}