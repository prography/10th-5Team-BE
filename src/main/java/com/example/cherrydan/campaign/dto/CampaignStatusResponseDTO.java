package com.example.cherrydan.campaign.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

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
    private int applicantCount;
    private int recruitCount;
    private List<String> snsPlatforms;
    private String campaignPlatform;
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
            case "registered":
                prefix = "리뷰 마감 ";
                break;
        }
        if (days > 0) return prefix + days + "일 전";
        if (days < 0) return prefix + Math.abs(days) + "일 지남";
        return "오늘" + prefix.replace(" ", "");
    }

    public static CampaignStatusResponseDTO fromEntity(com.example.cherrydan.campaign.domain.CampaignStatus status) {
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
            case REGISTERED:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "registered");
                break;
            case ENDED:
                reviewerAnnouncementStatus = getStatusMessage(status.getCampaign().getResultAnnouncement(), "ended");
                break;
            default:
                break;
        }
        return CampaignStatusResponseDTO.builder()
                .id(status.getId())
                .campaignId(status.getCampaign().getId())
                .userId(status.getUser().getId())
                .statusLabel(status.getStatus().getLabel())
                .title(status.getCampaign().getTitle())
                .detailUrl(status.getCampaign().getDetailUrl())
                .imageUrl(status.getCampaign().getImageUrl())
                .reviewerAnnouncement(status.getCampaign().getReviewerAnnouncement())
                .reviewerAnnouncementStatus(reviewerAnnouncementStatus)
                .applicantCount(status.getCampaign().getApplicantCount())
                .recruitCount(status.getCampaign().getRecruitCount())
                .snsPlatforms(com.example.cherrydan.campaign.dto.BookmarkResponseDTO.getPlatforms(status.getCampaign()))
                .campaignPlatform(com.example.cherrydan.campaign.dto.BookmarkResponseDTO.getCampaignPlatformLabel(status.getCampaign().getSourceSite()))
                .benefit(status.getCampaign().getBenefit())
                .contentSubmissionEnd(status.getCampaign().getContentSubmissionEnd())
                .resultAnnouncement(status.getCampaign().getResultAnnouncement())
                .build();
    }
} 