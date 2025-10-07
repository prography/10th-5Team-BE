package com.example.cherrydan.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UnreadAlertCountResponseDTO {

    @Schema(description = "총 미읽은 알림 개수", example = "15", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long totalCount;

    @Schema(description = "활동 알림 미읽은 개수", example = "8", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long activityAlertCount;

    @Schema(description = "키워드 알림 미읽은 개수", example = "7", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long keywordAlertCount;
}
