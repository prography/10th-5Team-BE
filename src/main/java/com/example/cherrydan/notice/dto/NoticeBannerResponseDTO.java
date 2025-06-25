package com.example.cherrydan.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeBannerResponseDTO {
    private Long id;
    private String title;
    private String imageUrl;
    private String bannerType; // NOTICE, EVENT, AD 등
    private String linkType;   // INTERNAL, EXTERNAL
    private Long targetId;     // 내부 이동용(상세 id)
    private String targetUrl;  // 외부 이동용(광고 url)
    private LocalDateTime updatedAt;
} 