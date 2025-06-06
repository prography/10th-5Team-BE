package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.domain.Region;
import com.example.cherrydan.campaign.domain.RegionCategory;
import com.example.cherrydan.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum 정보 제공 API
 */
@RestController
@RequestMapping("/api/enums")
@RequiredArgsConstructor
@Tag(name = "Enum", description = "Enum 정보 조회 API")
public class EnumController {

    /**
     * 지역 enum 정보 조회
     */
    @GetMapping("/regions")
    @Operation(summary = "지역 enum 조회", description = "사용 가능한 모든 지역 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<List<EnumInfo>>> getRegions() {
        List<EnumInfo> regions = Arrays.stream(Region.values())
                .map(region -> new EnumInfo(region.name(), region.getCode(), region.getDescription()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(regions));
    }

    /**
     * 지역 카테고리 enum 정보 조회
     */
    @GetMapping("/region-categories")
    @Operation(summary = "지역 카테고리 enum 조회", description = "사용 가능한 모든 지역 카테고리 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<List<EnumInfo>>> getRegionCategories() {
        List<EnumInfo> categories = Arrays.stream(RegionCategory.values())
                .map(category -> new EnumInfo(category.name(), category.getCode(), category.getDescription()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Enum 정보 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class EnumInfo {
        private String value;       // enum 상수명 (API에서 사용할 값)
        private String code;        // 한글 코드
        private String description; // 설명
    }
}
