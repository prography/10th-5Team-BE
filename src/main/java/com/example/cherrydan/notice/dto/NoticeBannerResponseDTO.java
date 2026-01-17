package com.example.cherrydan.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeBannerResponseDTO {
    private Long id;
    private String title;
    private String subTitle;
    private String backgroundColor;
    private String bannerType; // NOTICE, EVENT, AD 등
    private String linkType;   // INTERNAL, EXTERNAL
    @Schema(description = "내부 이동용(상세 id)", nullable = true)
    private Long targetId;     // 내부 이동용(상세 id)
    @Schema(description = "외부 이동용 URL", nullable = true)
    private String targetUrl;  // 외부 이동용 URL
} 