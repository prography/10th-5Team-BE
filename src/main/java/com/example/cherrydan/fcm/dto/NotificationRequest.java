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
      "title": "새로운 채팅",
      "body": "홍길동: 안녕하세요! 아직 판매 중인가요?",
      "imageUrl": "https://example.com/product-image.jpg",
      "data": {
        "type": "chat_message",
        "chat_room_id": "room_12345",
        "sender_name": "홍길동",
        "product_id": "prod_456",
        "action": "open_chat"
      },
      "androidIcon": "ic_chat",
      "androidColor": "#32C832",
      "iosBadge": 1,
      "priority": "high"
    }
    """
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    @Schema(description = "알림 제목", example = "새로운 채팅", required = true)
    private String title;
    
    @Schema(description = "알림 내용", example = "홍길동: 안녕하세요! 아직 판매 중인가요?", required = true)
    private String body;
    
    @Schema(description = "알림 이미지 URL (HTTPS만 지원)", example = "https://example.com/product-image.jpg")
    private String imageUrl;
    
    @Schema(
        description = "추가 데이터 (key-value 형태, 백그라운드에서도 전달됨)", 
        example = """
        {
          "type": "chat_message",
          "chat_room_id": "room_12345",
          "sender_name": "홍길동",
          "product_id": "prod_456",
          "action": "open_chat"
        }
        """
    )
    private Map<String, String> data = new HashMap<>();
    
    // Android 전용 설정
    @Schema(description = "안드로이드 알림 아이콘", example = "ic_notification")
    private String androidIcon;
    
    @Schema(description = "안드로이드 알림 색상 (HEX 코드)", example = "#FF6B6B")
    private String androidColor;
    
    @Schema(description = "안드로이드 알림 사운드", example = "default")
    private String androidSound = "default";
    
    @Schema(description = "안드로이드 클릭 액션", example = "FLUTTER_NOTIFICATION_CLICK")
    private String clickAction;
    
    // iOS 전용 설정
    @Schema(description = "iOS 알림 사운드", example = "default")
    private String iosSound = "default";
    
    @Schema(description = "iOS 앱 아이콘 뱃지 숫자", example = "1")
    private Integer iosBadge;
    
    @Schema(description = "iOS 알림 카테고리", example = "MESSAGE")
    private String iosCategory;
    
    @Schema(description = "알림 우선순위 (high 또는 normal)", example = "high")
    private String priority = "high";
    
    @Schema(description = "알림 TTL (Time To Live, 초 단위)", example = "3600")
    private Long ttl;

    public static NotificationRequest createSimple(String title, String body) {
        return NotificationRequest.builder()
                .title(title)
                .body(body)
                .build();
    }
}
