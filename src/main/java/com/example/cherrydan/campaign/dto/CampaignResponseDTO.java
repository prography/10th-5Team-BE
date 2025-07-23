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
    private String campaignPlatformImageUrl;
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

    public static String getReviewerAnnouncementStatus(LocalDate reviewerAnnouncement) {
        if (reviewerAnnouncement == null) return null;
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, reviewerAnnouncement);
        if (days > 0) {
            return days + "일 남음";
        } else if (days < 0) {
            return Math.abs(days) + "일 지남";
        } else {
            return "오늘 발표";
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
        return CampaignResponseDTO.builder()
            .id(campaign.getId())
            .title(campaign.getTitle())
            .detailUrl(campaign.getDetailUrl())
            .benefit(campaign.getBenefit())
            .reviewerAnnouncementStatus(getReviewerAnnouncementStatus(campaign.getReviewerAnnouncement()))
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
            .campaignType(campaign.getCampaignType())
            .competitionRate(campaign.getCompetitionRate())
            .isBookmarked(isBookmarked)
            .build();
    }
} 