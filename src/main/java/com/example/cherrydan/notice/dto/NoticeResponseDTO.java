// src/main/java/com/example/cherrydan/notice/dto/NoticeResponse.java
package com.example.cherrydan.notice.dto;

import com.example.cherrydan.notice.domain.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String category;
    private Boolean isPinned;
    private Integer viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public static NoticeResponseDTO from(Notice notice) {
        return NoticeResponseDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .category(notice.getCategory().getDescription())
                .isPinned(notice.getIsPinned())
                .viewCount(notice.getViewCount())
                .publishedAt(notice.getPublishedAt())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
