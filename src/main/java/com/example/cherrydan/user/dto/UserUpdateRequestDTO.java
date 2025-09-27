package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보 수정 요청")
public class UserUpdateRequestDTO {
    @Schema(description = "닉네임", example = "cherrydan", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String nickname;
    @Schema(description = "출생년도", example = "1990", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer birthYear;
    @Schema(description = "성별", example = "MALE", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String gender;
} 