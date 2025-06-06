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

    @Schema(description = "지역", 
            example = "SEOUL", 
            required = true,
            allowableValues = {"ALL", "SEOUL", "SEOUL_GANGNAM_NONHYEON", "SEOUL_GANGDONG_CHEONHO", 
                              "BUSAN", "BUSAN_HAEUNDAE_CENTUM", "DAEGU", "INCHEON", "GWANGJU", 
                              "DAEJEON", "ULSAN", "SEJONG", "GYEONGGI", "GANGWON", "CHUNGBUK", 
                              "CHUNGNAM", "JEONBUK", "JEONNAM", "GYEONGBUK", "GYEONGNAM", "JEJU"})
    private Region region;

    @Schema(description = "지역 카테고리", 
            example = "RESTAURANT", 
            required = true,
            allowableValues = {"ALL", "RESTAURANT", "BEAUTY", "ACCOMMODATION", "CULTURE", "DELIVERY", "PACKAGING", "ETC"})
    private RegionCategory regionCategory;

    /**
     * 유효성 검증
     */
    public boolean isValid() {
        return region != null && regionCategory != null;
    }
}