package com.example.cherrydan.campaign.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.temporal.ChronoUnit;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.common.util.CloudfrontUtil;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
public class CampaignResponseDTO {
    @Schema(description = "캠페인 ID", example = "1", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "캠페인 제목", example = "[양주] 리치마트 양주점_피드&릴스", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "캠페인 상세 URL", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String detailUrl;

    @Schema(description = "혜택 정보", example = "5만원 상당 체험권 제공", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String benefit;

    @Schema(description = "마감 상태 메시지", example = "신청 마감 3일 전", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reviewerAnnouncementStatus;

    @Schema(description = "신청자 수", example = "150", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer applicantCount;

    @Schema(description = "모집 인원", example = "10", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer recruitCount;

    @Deprecated
    @Schema(description = "플랫폼 이름(deprecated)", deprecated = true, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("campaignSite")
    private String sourceSite;

    @Schema(description = "캠페인 이미지 URL", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String imageUrl;

    @JsonIgnore private Boolean youtube;
    @JsonIgnore private Boolean shorts;
    @JsonIgnore private Boolean insta;
    @JsonIgnore private Boolean reels;
    @JsonIgnore private Boolean blog;
    @JsonIgnore private Boolean clip;
    @JsonIgnore private Boolean tiktok;
    @JsonIgnore private Boolean thread;
    @JsonIgnore private Boolean etc;

    @Schema(description = "북마크 여부", example = "false", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isBookmarked;

    @Deprecated
    @Schema(description = "기존 플랫폼 이미지 URL(deprecated)", deprecated = true, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String campaignPlatformImageUrl;

    @Schema(description = "캠페인 플랫폼 이미지 URL", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteUrl;

    @Schema(description = "캠페인 플랫폼 한글명", example = "레뷰", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteKr;

    @Schema(description = "캠페인 플랫폼 영문 코드", example = "revu", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignSiteEn;

    @Schema(description = "캠페인 타입", example = "DELIVERY", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CampaignType campaignType;

    @Schema(description = "경쟁률", example = "15.0", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Float competitionRate;

    @JsonProperty("snsPlatforms")
    public List<String> getSnsPlatforms() {
        List<String> platforms = new ArrayList<>();
        if (Boolean.TRUE.equals(youtube)) platforms.add(SnsPlatformType.YOUTUBE.getLabel());
        if (Boolean.TRUE.equals(shorts)) platforms.add(SnsPlatformType.SHORTS.getLabel());
        if (Boolean.TRUE.equals(insta)) platforms.add(SnsPlatformType.INSTAGRAM.getLabel());
        if (Boolean.TRUE.equals(reels)) platforms.add(SnsPlatformType.REELS.getLabel());
        if (Boolean.TRUE.equals(blog)) platforms.add(SnsPlatformType.BLOG.getLabel());
        if (Boolean.TRUE.equals(clip)) platforms.add(SnsPlatformType.CLIP.getLabel());
        if (Boolean.TRUE.equals(tiktok)) platforms.add(SnsPlatformType.TIKTOK.getLabel());
        if (Boolean.TRUE.equals(thread)) platforms.add(SnsPlatformType.THREAD.getLabel());
        if (Boolean.TRUE.equals(etc)) platforms.add(SnsPlatformType.ETC.getLabel());
        return platforms;
    }

    public static String getReviewerAnnouncementStatus(LocalDate applyEnd) {
        if (applyEnd == null) return null;
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, applyEnd);
        if (days > 0) {
            return "신청 마감 " + days + "일 전";
        } else if (days < 0) {
            return "모집이 종료되었어요";
        } else {
            return "오늘이 마감일!";
        }
    }

    public static String toPlatformLabel(String code) {
        if (code == null) return null;
        String trimmedCode = code.trim();
        try {
            return CampaignPlatformType.fromCode(trimmedCode).getLabel();
        } catch (IllegalArgumentException e) {
            return trimmedCode;
        }
    }

    public static CampaignResponseDTO fromEntityWithBookmark(Campaign campaign, boolean isBookmarked) {
        String campaignPlatformImageUrl = CloudfrontUtil.getCampaignPlatformImageUrl(campaign.getSourceSite());
        String campaignSiteUrl = CloudfrontUtil.getCampaignPlatformImageUrl(campaign.getSourceSite());
        CampaignPlatformType platformType = CampaignPlatformType.fromCode(campaign.getSourceSite());
        return CampaignResponseDTO.builder()
            .id(campaign.getId())
            .title(campaign.getTitle())
            .detailUrl(campaign.getDetailUrl())
            .benefit(campaign.getBenefit())
            .reviewerAnnouncementStatus(getReviewerAnnouncementStatus(campaign.getApplyEnd()))
            .applicantCount(campaign.getApplicantCount())
            .recruitCount(campaign.getRecruitCount())
            .sourceSite(toPlatformLabel(campaign.getSourceSite()))
            .imageUrl(campaign.getImageUrl())
            .youtube(campaign.getYoutube())
            .shorts(campaign.getShorts())
            .insta(campaign.getInsta())
            .reels(campaign.getReels())
            .blog(campaign.getBlog())
            .clip(campaign.getClip())
            .tiktok(campaign.getTiktok())
            .thread(campaign.getThread())
            .etc(campaign.getEtc())
            .campaignPlatformImageUrl(campaignPlatformImageUrl)
            .campaignSiteUrl(campaignSiteUrl)
            .campaignType(campaign.getCampaignType())
            .competitionRate(campaign.getCompetitionRate())
            .isBookmarked(isBookmarked)
            .campaignSiteKr(platformType.getLabel())
            .campaignSiteEn(platformType.getCode())
            .build();
    }
} 