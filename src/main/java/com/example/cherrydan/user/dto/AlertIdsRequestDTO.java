package com.example.cherrydan.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 ID 리스트 요청")
public class AlertIdsRequestDTO {
    @Schema(description = "알림 ID 리스트", example = "[1, 2, 3]", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> alertIds;
}