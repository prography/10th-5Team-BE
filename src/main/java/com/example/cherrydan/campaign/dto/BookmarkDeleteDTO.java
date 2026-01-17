package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;


@Getter
@NoArgsConstructor
@Schema(description = "북마크 삭제 요청 DTO")
public class BookmarkDeleteDTO {

    @NotEmpty(message = "캠페인 ID 목록은 필수입니다.")
    @Schema(description = "북마크 삭제 DTO ", example = "[1, 2, 3]")
    private List<Long> campaignIds;
} 