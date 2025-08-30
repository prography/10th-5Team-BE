package com.example.cherrydan.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
public class BookmarkDeleteDTO {
    @Schema(description = "북마크 삭제 DTO ", example = "[1, 2, 3]")
    private List<Long> campaignIds;
} 