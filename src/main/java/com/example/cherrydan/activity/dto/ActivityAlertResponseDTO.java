package com.example.cherrydan.activity.dto;

import com.example.cherrydan.activity.domain.ActivityAlert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "활동 알림 응답 DTO")
public class ActivityAlertResponseDTO {
    
    @Schema(description = "알림 ID", example = "1")
    private Long id;
    
    @Schema(description = "캠페인 ID", example = "123")
    private Long campaignId;
    
    @Schema(description = "캠페인 제목", example = "[양주] 리치마트 양주점_피드&릴스")
    private String campaignTitle;
    
    @Schema(description = "신청 마감일", example = "2024-07-24")
    private LocalDate applyEndDate;
    
    @Schema(description = "알림 날짜", example = "2024-07-21")
    private LocalDate alertDate;
    
    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;
    
    @Schema(description = "D-day (마감까지 남은 일수)", example = "3")
    private Integer dDay;

    public static ActivityAlertResponseDTO fromEntity(ActivityAlert activityAlert) {
        final int FIXED_D_DAY = 3;
        LocalDate applyEndDate = activityAlert.getCampaign().getApplyEnd();
        
        return ActivityAlertResponseDTO.builder()
                .id(activityAlert.getId())
                .campaignId(activityAlert.getCampaign().getId())
                .campaignTitle(activityAlert.getCampaign().getTitle())
                .applyEndDate(applyEndDate)
                .alertDate(activityAlert.getAlertDate())
                .isRead(activityAlert.getIsRead())
                .dDay(FIXED_D_DAY)
                .build();
    }
}