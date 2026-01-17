package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.UserKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 키워드 응답")
public class UserKeywordResponseDTO {
    @Schema(description = "키워드 ID", example = "1", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "키워드", example = "뷰티", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    public static UserKeywordResponseDTO fromKeyword(UserKeyword keyword) {
        return UserKeywordResponseDTO.builder()
                .id(keyword.getId())
                .keyword(keyword.getKeyword())
                .build();
    }
} 