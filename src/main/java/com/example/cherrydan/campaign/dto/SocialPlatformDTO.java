package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜미디어 플랫폼 필터 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "소셜미디어 플랫폼 필터")
public class SocialPlatformDTO {

    @Schema(description = "유튜브 포함 여부", example = "1")
    private Integer youtube;
    
    @Schema(description = "쇼츠 포함 여부", example = "1")
    private Integer shorts;
    
    @Schema(description = "인스타그램 포함 여부", example = "1")
    private Integer insta;
    
    @Schema(description = "릴스 포함 여부", example = "0")
    private Integer reels;
    
    @Schema(description = "블로그 포함 여부", example = "1")
    private Integer blog;

    @Schema(description = "클립 포함 여부", example = "1")
    private Integer clip;
    
    @Schema(description = "틱톡 포함 여부", example = "0")
    private Integer tiktok;
    
    @Schema(description = "기타 포함 여부", example = "0")
    private Integer etc;

    /**
     * 소셜미디어 필터가 있는지 확인
     */
    public boolean hasAnyFilter() {
        return youtube != null || shorts != null || insta != null || reels != null ||
               blog != null || clip != null || tiktok != null || etc != null;
    }
}