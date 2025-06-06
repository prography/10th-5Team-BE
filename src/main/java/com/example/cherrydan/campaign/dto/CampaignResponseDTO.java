package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SocialPlatforms;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 캠페인 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캠페인 응답 정보")
public class CampaignResponseDTO {

    @Schema(description = "캠페인 ID", example = "1")
    private Long id;

    @Schema(description = "캠페인 제목", example = "서울 맛집 체험단 모집")
    private String title;

    @Schema(description = "상세 URL", example = "https://example.com/campaign/1")
    private String detailUrl;

    @Schema(description = "혜택 내용", example = "무료 식사 제공 + 체험비 10만원")
    private String benefit;

    @Schema(description = "캠페인 타입", example = "REGIONAL")
    private String campaignType;

    @Schema(description = "캠페인 타입 설명", example = "지역(방문)")
    private String campaignTypeDescription;

    @Schema(description = "신청 시작일", example = "2025-06-01")
    private LocalDate applyStart;

    @Schema(description = "신청 마감일", example = "2025-06-15")
    private LocalDate applyEnd;

    @Schema(description = "모집 인원", example = "20")
    private Integer recruitCount;

    @Schema(description = "신청자 수", example = "85")
    private Integer applicantCount;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "출처 사이트", example = "리뷰어스")
    private String sourceSite;

    @Schema(description = "신청 가능 여부", example = "true")
    private Boolean isApplicable;

    @Schema(description = "소셜미디어 플랫폼 정보")
    private SocialPlatforms socialPlatforms;

    /**
     * Campaign Entity를 DTO로 변환
     */
    public static CampaignResponseDTO from(Campaign campaign) {
        CampaignType type = campaign.getCampaignTypeEnum();
        
        return CampaignResponseDTO.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .detailUrl(campaign.getDetailUrl())
                .benefit(campaign.getBenefit())
                .campaignType(type != null ? type.name() : null)
                .campaignTypeDescription(type != null ? type.getDescription() : null)
                .applyStart(campaign.getApplyStart())
                .applyEnd(campaign.getApplyEnd())
                .recruitCount(campaign.getRecruitCount())
                .applicantCount(campaign.getApplicantCount())
                .imageUrl(campaign.getImageUrl())
                .sourceSite(campaign.getSourceSite())
                .isApplicable(campaign.isApplicable())
                .socialPlatforms(campaign.getSocialPlatforms())
                .build();
    }

    /**
     * Campaign Entity 리스트를 DTO 리스트로 변환
     */
    public static List<CampaignResponseDTO> fromList(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(CampaignResponseDTO::from)
                .toList();
    }
}
