package com.example.cherrydan.activity.dto;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "활동 알림 응답 DTO")
public class ActivityNotificationResponseDTO {
    
    @Schema(description = "캠페인 상태 ID", example = "1")
    private Long campaignStatusId;
    
    @Schema(description = "알림 타입", example = "방문알림")
    private String notificationType;
    
    @Schema(description = "캠페인 제목", example = "체리단 카페 방문 후기 작성")
    private String notificationBoldText;
    
    @Schema(description = "전체 알림 텍스트", example = "D-3 [양주] 리치마트 양주점_피드&릴스 방문일이 3일 남았습니다.")
    private String fullText;
    
    @Schema(description = "읽음 여부 (false: 읽지 않음, true: 읽음)", example = "false")
    private Boolean isRead;
    
    @Schema(description = "오늘 날짜", example = "2025-06-29")
    private LocalDate today;
    
    /**
     * CampaignStatus를 ActivityNotificationResponseDTO로 변환
     */
    public static ActivityNotificationResponseDTO fromEntity(CampaignStatus campaignStatus) {
        var campaign = campaignStatus.getCampaign();
        int daysRemaining = campaignStatus.getDaysRemaining() != null ? campaignStatus.getDaysRemaining() : 0;
        
        // 캠페인 타입에 따른 알림 타입 결정
        String notificationType = getNotificationTypeByCategory(campaign.getCampaignType());
        
        // 전체 텍스트 생성
        String fullText = generateFullText(campaign.getTitle(), daysRemaining);
        
        return ActivityNotificationResponseDTO.builder()
                .campaignStatusId(campaignStatus.getId())
                .notificationType(notificationType)
                .notificationBoldText(campaign.getTitle())
                .fullText(fullText)
                .isRead(campaignStatus.getIsRead())
                .today(LocalDate.now())
                .build();
    }
    
    /**
     * 캠페인 타입에 따른 알림 타입 반환
     */
    private static String getNotificationTypeByCategory(CampaignType campaignType) {
        if (campaignType == null) {
            return "캠페인알림";
        }
        
        switch (campaignType) {
            case REGION:
                return "방문알림";
            case PRODUCT:
                return "제품알림";
            case REPORTER:
                return "기자단알림";
            default:
                return "캠페인알림";
        }
    }
    
    /**
     * 전체 알림 텍스트 생성
     */
    private static String generateFullText(String campaignTitle, int daysRemaining) {
        String dayText;
        if (daysRemaining == 0) {
            dayText = "D-DAY";
        } else {
            dayText = "D-" + daysRemaining;
        }
        
        String remainingText;
        if (daysRemaining == 0) {
            remainingText = "오늘 마감입니다.";
        } else if (daysRemaining == 1) {
            remainingText = "1일 남았습니다.";
        } else {
            remainingText = daysRemaining + "일 남았습니다.";
        }
        
        return String.format("%s %s 방문일이 %s", dayText, campaignTitle, remainingText);
    }
} 