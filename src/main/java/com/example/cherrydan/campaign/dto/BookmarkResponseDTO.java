package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import com.example.cherrydan.common.util.CloudfrontUtil;

@Getter
@Builder
public class BookmarkResponseDTO {
    private Long id;
    private Long campaignId;
    private Long userId;
    private String reviewerAnnouncementStatus;
    private String subStatusLabel;
    private String campaignTitle;
    private String benefit;
    private String campaignDetailUrl;
    private String campaignImageUrl;
    private String campaignPlatformImageUrl;
    private Integer applicantCount;
    private Integer recruitCount;
    private List<String> snsPlatforms;
    private String campaignSite;

    public static BookmarkResponseDTO fromEntity(Bookmark bookmark) {
        Campaign campaign = bookmark.getCampaign();
        String campaignPlatformImageUrl = CloudfrontUtil.getCampaignPlatformImageUrl(campaign.getSourceSite());
        return BookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUser().getId())
                .campaignId(campaign.getId())
                .campaignTitle(campaign.getTitle())
                .campaignDetailUrl(campaign.getDetailUrl())
                .campaignImageUrl(campaign.getImageUrl())
                .campaignPlatformImageUrl(campaignPlatformImageUrl)
                .benefit(campaign.getBenefit())
                .applicantCount(campaign.getApplicantCount())
                .recruitCount(campaign.getRecruitCount())
                .snsPlatforms(getPlatforms(campaign))
                .reviewerAnnouncementStatus(getReviewerAnnouncementStatus(campaign.getReviewerAnnouncement()))
                .campaignSite(getCampaignSiteLabel(campaign.getSourceSite()))
                .build();
    }

    public static List<String> getPlatforms(Campaign campaign) {
        List<String> platforms = new ArrayList<>();
        if (Boolean.TRUE.equals(campaign.getYoutube())) platforms.add(SnsPlatformType.YOUTUBE.getLabel());
        if (Boolean.TRUE.equals(campaign.getShorts())) platforms.add(SnsPlatformType.SHORTS.getLabel());
        if (Boolean.TRUE.equals(campaign.getInsta())) platforms.add(SnsPlatformType.INSTAGRAM.getLabel());
        if (Boolean.TRUE.equals(campaign.getReels())) platforms.add(SnsPlatformType.REELS.getLabel());
        if (Boolean.TRUE.equals(campaign.getBlog())) platforms.add(SnsPlatformType.BLOG.getLabel());
        if (Boolean.TRUE.equals(campaign.getClip())) platforms.add(SnsPlatformType.CLIP.getLabel());
        if (Boolean.TRUE.equals(campaign.getTiktok())) platforms.add(SnsPlatformType.TIKTOK.getLabel());
        if (Boolean.TRUE.equals(campaign.getThread())) platforms.add(SnsPlatformType.THREAD.getLabel());
        if (Boolean.TRUE.equals(campaign.getEtc())) platforms.add(SnsPlatformType.ETC.getLabel());
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

    public static String getCampaignSiteLabel(String code) {
        if (code == null) return null;
        try {
            return CampaignPlatformType.fromCode(code).getLabel();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }
} 