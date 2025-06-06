package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.Region;
import com.example.cherrydan.campaign.domain.RegionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역별 캠페인 검색 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "지역별 캠페인 검색 조건")
public class RegionSearchDTO {

    @Schema(description = "지역", example = "SEOUL", required = true)
    private Region region;

    @Schema(description = "지역 카테고리", example = "RESTAURANT", required = true)
    private RegionCategory regionCategory;

    /**
     * 유효성 검증
     */
    public boolean isValid() {
        return region != null && regionCategory != null;
    }
}