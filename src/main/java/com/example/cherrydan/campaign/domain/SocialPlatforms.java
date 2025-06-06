package com.example.cherrydan.campaign.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 소셜미디어 플랫폼 정보 (임베디드 값 객체)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Schema(description = "소셜미디어 플랫폼 정보")
public class SocialPlatforms {

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
     * 플랫폼 활성화 여부 확인
     */
    private boolean isActive(Integer platform) {
        return Integer.valueOf(1).equals(platform);
    }
}