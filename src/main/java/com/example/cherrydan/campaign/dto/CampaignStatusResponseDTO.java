package com.example.cherrydan.campaign.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.cherrydan.common.util.CloudfrontUtil;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "캠페인 상태 응답 DTO")
public class CampaignStatusResponseDTO {
    private Long id;
    private Long campaignId;
    private Long userId;
    private String reviewerAnnouncementStatus;
    private String statusLabel;
    private String title;
    private String benefit;
    private String detailUrl;
    private String imageUrl;
    private String campaignPlatformImageUrl;
    private int applicantCount;
    private int recruitCount;
    private List<String> snsPlatforms;
    private String campaignSite;
    @JsonIgnore private LocalDate reviewerAnnouncement;
    @JsonIgnore private LocalDate contentSubmissionEnd;
    @JsonIgnore private LocalDate resultAnnouncement;

    public static String getStatusMessage(LocalDate date, String type) {
        if (date == null) return null;
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, date);
        String prefix = "";
        switch (type) {
            case "bookmark":
                prefix = "신청 마감 ";
                break;
            case "apply":
            case "ended":
                prefix = "발표 ";
                break;
            case "selected":
            case "not_selected":
                prefix = "방문 마감 ";
                break;
            case "reviewing":
                prefix = "리뷰 마감 ";
                break;
        }
        if (days > 0) return prefix + days + "일 전";
        if (days < 0) return prefix + Math.abs(days) + "일 지남";
        return prefix + "D-Day"; 
    }

    public static CampaignStatusResponseDTO fromEntity(CampaignStatus status) {
        String reviewerAnnouncementStatus = null;
        switch (status.getStatus()) {
            case APPLY:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getReviewerAnnouncement(), "apply");
                break;
            case SELECTED:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "selected");
                break; 
            case NOT_SELECTED:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "not_selected");
                break;
            case REVIEWING:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "reviewing");
                break;
            case ENDED:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getResultAnnouncement(), "ended");
                break;
            default:
                break;
        }

        String campaignPlatformImageUrl = CloudfrontUtil.getCampaignPlatformImageUrl(status.getCampaign().getSourceSite());
        return CampaignStatusResponseDTO.builder()
                .id(status.getId())
                .campaignId(status.getCampaign().getId())
                .userId(status.getUser().getId())
                .statusLabel(status.getStatus().getLabel())
                .title(status.getCampaign().getTitle())
                .detailUrl(status.getCampaign().getDetailUrl())
                .imageUrl(status.getCampaign().getImageUrl())
                .campaignPlatformImageUrl(campaignPlatformImageUrl)
                .reviewerAnnouncement(status.getCampaign().getReviewerAnnouncement())
                .reviewerAnnouncementStatus(reviewerAnnouncementStatus)
                .applicantCount(status.getCampaign().getApplicantCount())
                .recruitCount(status.getCampaign().getRecruitCount())
                .snsPlatforms(BookmarkResponseDTO.getPlatforms(status.getCampaign()))
                .campaignSite(BookmarkResponseDTO.getCampaignSiteLabel(status.getCampaign().getSourceSite()))
                .benefit(status.getCampaign().getBenefit())
                .contentSubmissionEnd(status.getCampaign().getContentSubmissionEnd())
                .resultAnnouncement(status.getCampaign().getResultAnnouncement())
                .build();
    }
} 