package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.ActivityNotificationResponseDTO;
import java.util.List;

public interface ActivityService {
    
    /**
     * 사용자의 활동 알림 목록 조회
     */
    List<ActivityNotificationResponseDTO> getActivityNotifications(Long userId);
    
    /**
     * 활동 알림 읽음 처리 (단일)
     */
    void markNotificationAsRead(Long userId, Long campaignStatusId);
    
    /**
     * 활동 알림 읽음 처리 (여러개)
     */
    void markNotificationsAsRead(Long userId, List<Long> campaignStatusIds);
    
    /**
     * 활동 알림 발송
     */
    void sendActivityNotifications();

    /**
     * 활동 알림(캠페인 상태) 개별 삭제 (isActive = false)
     */
    void deleteActivityAlert(Long userId, Long alertId);
} 