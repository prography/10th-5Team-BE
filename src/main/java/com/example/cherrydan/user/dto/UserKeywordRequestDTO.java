package com.example.cherrydan.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 키워드 요청")
public class UserKeywordRequestDTO {
    @Schema(description = "키워드", example = "뷰티", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;
}