package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.CampaignStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignStatusPopupItemDTO {
    private Long campaignId;
    private String title;
    private String imageUrl;
    private String reviewerAnnouncementStatus;
    private String benefit;

    public static CampaignStatusPopupItemDTO fromEntity(CampaignStatus status) {
        String reviewerAnnouncementStatus = null;
        switch (status.getStatus()) {
            case APPLY:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getReviewerAnnouncement(), "apply");
                break;
            case SELECTED:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "selected");
                break;
            case REVIEWING:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "reviewing");
                break;
            case ENDED:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getResultAnnouncement(), "ended");
                break;
            default:
                break;
        }
        return CampaignStatusPopupItemDTO.builder()
                .campaignId(status.getCampaign().getId())
                .title(status.getCampaign().getTitle())
                .imageUrl(status.getCampaign().getImageUrl())
                .reviewerAnnouncementStatus(reviewerAnnouncementStatus)
                .benefit(status.getCampaign().getBenefit())
                .build();
    }
} 