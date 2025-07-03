package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "키워드 캠페인 알림 응답 DTO")
public class KeywordCampaignAlertResponseDTO {
    
    @Schema(description = "알림 ID", example = "1")
    private Long id;
    
    @Schema(description = "매칭된 키워드", example = "뷰티")
    private String keyword;
    
    @Schema(description = "읽음 상태", example = "false")
    private Boolean isRead;
    
    /**
     * 엔티티를 DTO로 변환
     */
    public static KeywordCampaignAlertResponseDTO fromEntity(KeywordCampaignAlert alert) {
        return KeywordCampaignAlertResponseDTO.builder()
                .id(alert.getId())
                .keyword(alert.getKeyword())
                .isRead(alert.getIsRead())
                .build();
    }
} 