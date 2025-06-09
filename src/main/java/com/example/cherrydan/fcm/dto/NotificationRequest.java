package com.example.cherrydan.fcm.dto;

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
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data = new HashMap<>();
    
    // Android 전용 설정
    private String androidIcon;
    private String androidColor;
    private String androidSound = "default";
    private String clickAction;
    
    // iOS 전용 설정
    private String iosSound = "default";
    private Integer iosBadge;
    private String iosCategory;
    
    private String priority = "high";
    private Long ttl;

    public static NotificationRequest createSimple(String title, String body) {
        return NotificationRequest.builder()
                .title(title)
                .body(body)
                .build();
    }
}
