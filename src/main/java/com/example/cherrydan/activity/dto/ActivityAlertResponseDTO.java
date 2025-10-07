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

    @Schema(description = "알림 ID", example = "1", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "캠페인 ID", example = "123", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long campaignId;

    @Schema(description = "알림 타이틀", example = "방문 알림", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String alertTitle;

    @Schema(description = "dDay 알림 타이틀", example = "D-3", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayTitle;

    @Schema(description = "캠페인 타이틀", example = "[양주] 리치마트 양주점_피드&릴스", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignTitle;

    @Schema(description = "며칠 남았는 지", example = "피드&릴스 방문일이 3일 남았습니다.", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String alertBody;

    @Schema(description = "알림 날짜", example = "2024-07-21", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate alertDate;

    @Schema(description = "읽음 여부", example = "false", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isRead;

    public static ActivityAlertResponseDTO fromEntity(ActivityAlert activityAlert) {
        return ActivityAlertResponseDTO.builder()
                .id(activityAlert.getId())
                .campaignId(activityAlert.getCampaign().getId())
                .alertTitle(activityAlert.getAlertType().getTitle())
                .dayTitle(activityAlert.getAlertType().getDDayLabel())
                .campaignTitle(activityAlert.getCampaign().getTitle())
                .alertBody(activityAlert.getAlertType().getMessageTemplate())
                .alertDate(activityAlert.getAlertDate())
                .isRead(activityAlert.getIsRead())
                .build();
    }
}