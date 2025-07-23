package com.example.cherrydan.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 알림 전송 요청 DTO
 * FCM 푸시 알림 전송을 위한 요청 데이터를 담는 클래스
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
@Schema(
    description = "FCM 알림 전송 요청", 
    example = """
    {
      "title": "새로운 알림",
      "body": "새로운 캠페인이 등록되었습니다!",
      "imageUrl": "https://example.com/campaign-image.jpg",
      "data": {
        "type": "campaign",
        "campaign_id": "12345",
        "action": "open_campaign"
      },
      "priority": "high"
    }
    """
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    @Schema(description = "알림 제목", example = "새로운 알림", required = true)
    private String title;
    
    @Schema(description = "알림 내용", example = "새로운 캠페인이 등록되었습니다!", required = true)
    private String body;
    
    @Schema(description = "알림 이미지 URL (HTTPS만 지원)", example = "https://example.com/campaign-image.jpg")
    private String imageUrl;
    
    @Schema(
        description = "추가 데이터 (key-value 형태)", 
        example = """
        {
          "type": "campaign",
          "campaign_id": "12345",
          "action": "open_campaign"
        }
        """
    )
    @Builder.Default
    private Map<String, String> data = new HashMap<>();
    
    @Schema(description = "알림 우선순위 (high 또는 normal)", example = "high")
    @Builder.Default
    private String priority = "high";

    public static NotificationRequest createSimple(String title, String body) {
        return NotificationRequest.builder()
                .title(title)
                .body(body)
                .build();
    }
}
