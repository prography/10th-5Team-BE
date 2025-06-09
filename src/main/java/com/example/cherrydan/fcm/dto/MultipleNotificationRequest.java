package com.example.cherrydan.fcm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 다중 사용자 알림 전송 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class MultipleNotificationRequest {
    
    private List<Long> userIds;
    private NotificationRequest notification;
}
