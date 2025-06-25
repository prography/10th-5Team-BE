package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class BookmarkResponseDTO {
    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private String benefit;
    private Integer applicantCount;
    private Integer recruitCount;
    private List<String> snsPlatforms;
    private String reviewerAnnouncementStatus;
    private String campaignPlatform;

    public static BookmarkResponseDTO fromEntity(Bookmark bookmark) {
        Campaign campaign = bookmark.getCampaign();
        return BookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .campaignId(campaign.getId())
                .campaignTitle(campaign.getTitle())
                .benefit(campaign.getBenefit())
                .applicantCount(campaign.getApplicantCount())
                .recruitCount(campaign.getRecruitCount())
                .snsPlatforms(getPlatforms(campaign))
                .reviewerAnnouncementStatus(getReviewerAnnouncementStatus(campaign.getReviewerAnnouncement()))
                .campaignPlatform(getCampaignPlatformLabel(campaign.getSourceSite()))
                .build();
    }

    private static List<String> getPlatforms(Campaign campaign) {
        List<String> platforms = new ArrayList<>();
        if (Boolean.TRUE.equals(campaign.getYoutube())) platforms.add(SnsPlatformType.YOUTUBE.getLabel());
        if (Boolean.TRUE.equals(campaign.getShorts())) platforms.add("쇼츠");
        if (Boolean.TRUE.equals(campaign.getInsta())) platforms.add(SnsPlatformType.INSTAGRAM.getLabel());
        if (Boolean.TRUE.equals(campaign.getReels())) platforms.add("릴스");
        if (Boolean.TRUE.equals(campaign.getBlog())) platforms.add(SnsPlatformType.BLOG.getLabel());
        if (Boolean.TRUE.equals(campaign.getClip())) platforms.add("클립");
        if (Boolean.TRUE.equals(campaign.getTiktok())) platforms.add(SnsPlatformType.TIKTOK.getLabel());
        if (Boolean.TRUE.equals(campaign.getEtc())) platforms.add(SnsPlatformType.ETC.getLabel());
        return platforms;
    }

    private static String getReviewerAnnouncementStatus(LocalDate reviewerAnnouncement) {
        if (reviewerAnnouncement == null) return null;
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, reviewerAnnouncement);
        if (days > 0) {
            return "발표 " + days + "일 전";
        } else if (days < 0) {
            return "발표 " + Math.abs(days) + "일 지남";
        } else {
            return "오늘 발표";
        }
    }

    private static String getCampaignPlatformLabel(String code) {
        if (code == null) return null;
        try {
            return CampaignPlatformType.fromCode(code).getLabel();
        } catch (IllegalArgumentException e) {
            return code; // 매칭 안 되면 코드 그대로 반환
        }
    }
} 