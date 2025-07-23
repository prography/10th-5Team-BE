package com.example.cherrydan.notice.dto;

import com.example.cherrydan.notice.domain.Notice;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponseDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String category;
    private Boolean isHot;
    private Integer viewCount;
    private Integer empathyCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public static NoticeResponseDTO from(Notice notice) {
        return NoticeResponseDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrls(notice.getImageUrls())
                .category(notice.getCategory().getDescription())
                .isHot(notice.getIsHot())
                .viewCount(notice.getViewCount())
                .empathyCount(notice.getEmpathyCount())
                .publishedAt(notice.getPublishedAt())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
