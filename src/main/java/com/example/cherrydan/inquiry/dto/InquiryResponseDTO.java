package com.example.cherrydan.inquiry.dto;

import com.example.cherrydan.inquiry.domain.Inquiry;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDTO {
    private Long id;
    private String category;
    private String title;
    private String content;
    private String status;
    private String adminReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;

    public static InquiryResponseDTO from(Inquiry inquiry) {
        return InquiryResponseDTO.builder()
                .id(inquiry.getId())
                .category(inquiry.getCategory().getDescription())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .status(inquiry.getStatus().getDescription())
                .adminReply(inquiry.getAdminReply())
                .repliedAt(inquiry.getRepliedAt())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
