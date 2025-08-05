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
    private Long id;
    private String title;
    private String detailUrl;
    private String benefit;
    private String reviewerAnnouncementStatus;
    private Integer applicantCount;
    private Integer recruitCount;

    @Deprecated
    @Schema(description = "플랫폼 이름(deprecated(v1.0.2까지))", deprecated = true)
    @JsonProperty("campaignSite")
    private String sourceSite;
    private String imageUrl;
    @JsonIgnore private Boolean youtube;
    @JsonIgnore private Boolean shorts;
    @JsonIgnore private Boolean insta;
    @JsonIgnore private Boolean reels;
    @JsonIgnore private Boolean blog;
    @JsonIgnore private Boolean clip;
    @JsonIgnore private Boolean tiktok;
    @JsonIgnore private Boolean etc;
    private Boolean isBookmarked;

    @Deprecated
    @Schema(description = "기존 플랫폼 이미지 URL(deprecated(v1.0.2까지))", deprecated = true)
    private String campaignPlatformImageUrl;
    
    private String campaignSiteUrl;
    private String campaignSiteKr;
    private String campaignSiteEn;
    private CampaignType campaignType;
    private Float competitionRate;

    @JsonProperty("snsPlatforms")
    public List<String> getSnsPlatforms() {
        List<String> platforms = new ArrayList<>();
        if (Boolean.TRUE.equals(youtube)) platforms.add(SnsPlatformType.YOUTUBE.getLabel());
        if (Boolean.TRUE.equals(shorts)) platforms.add("쇼츠");
        if (Boolean.TRUE.equals(insta)) platforms.add(SnsPlatformType.INSTAGRAM.getLabel());
        if (Boolean.TRUE.equals(reels)) platforms.add("릴스");
        if (Boolean.TRUE.equals(blog)) platforms.add(SnsPlatformType.BLOG.getLabel());
        if (Boolean.TRUE.equals(clip)) platforms.add("클립");
        if (Boolean.TRUE.equals(tiktok)) platforms.add(SnsPlatformType.TIKTOK.getLabel());
        if (Boolean.TRUE.equals(etc)) platforms.add(SnsPlatformType.ETC.getLabel());
        return platforms;
    }

    public static String getReviewerAnnouncementStatus(LocalDate applyEnd) {
        if (applyEnd == null) return null;
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, applyEnd);
        if (days > 0) {
            return days + "일 남음";
        } else if (days < 0) {
            return Math.abs(days) + "일 지남";
        } else {
            return "오늘 마감";
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