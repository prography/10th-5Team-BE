package com.example.cherrydan.activity.dto;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ActivityCampaignResponseDTO {
    
    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private String campaignImageUrl;
    private String detailUrl;
    private String benefit;
    private Integer applicantCount;
    private Integer recruitCount;
    private List<String> snsPlatforms;
    private String campaignPlatform;
    
    // 활동 관련 정보
    private LocalDate targetDate; // 마감일
    private Integer daysRemaining; // 남은 일수
    private String statusLabel; // 상태 라벨 (신청, 선정, 등록 등)
    private String urgencyMessage; // "3일 남음", "오늘 마감" 등
    
    public static String getUrgencyMessage(int daysRemaining) {
        if (daysRemaining == 0) {
            return "오늘 마감";
        } else if (daysRemaining == 1) {
            return "내일 마감";
        } else if (daysRemaining == 2) {
            return "모레 마감";
        } else {
            return daysRemaining + "일 남음";
        }
    }
    
    /**
     * CampaignStatus를 DTO로 변환
     */
    public static ActivityCampaignResponseDTO fromEntity(CampaignStatus campaignStatus) {
        var campaign = campaignStatus.getCampaign();
        
        return ActivityCampaignResponseDTO.builder()
                .id(campaignStatus.getId())
                .campaignId(campaign.getId())
                .campaignTitle(campaign.getTitle())
                .campaignImageUrl(campaign.getImageUrl())
                .detailUrl(campaign.getDetailUrl())
                .benefit(campaign.getBenefit())
                .applicantCount(campaign.getApplicantCount())
                .recruitCount(campaign.getRecruitCount())
                .snsPlatforms(BookmarkResponseDTO.getPlatforms(campaign))
                .campaignPlatform(BookmarkResponseDTO.getCampaignPlatformLabel(campaign.getSourceSite()))
                .targetDate(campaignStatus.getActivityTargetDate())
                .daysRemaining(campaignStatus.getDaysRemaining())
                .statusLabel(campaignStatus.getStatus().getLabel())
                .urgencyMessage(getUrgencyMessage(campaignStatus.getDaysRemaining()))
                .build();
    }
} 