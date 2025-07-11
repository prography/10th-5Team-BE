package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.ActivityNotificationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ActivityService {
    
    /**
     * 사용자의 활동 알림 목록 조회 (페이지네이션)
     */
    Page<ActivityNotificationResponseDTO> getActivityNotifications(Long userId, Pageable pageable);
    
    /**
     * 활동 알림 읽음 처리 (배열)
     * 1개 또는 여러개 모두 배열로 처리
     */
    void markNotificationsAsRead(Long userId, List<Long> campaignStatusIds);
    
    /**
     * 활동 알림 발송
     */
    void sendActivityNotifications();

    /**
     * 활동 알림(캠페인 상태) 삭제 (isActive = false)
     * 1개 또는 여러개 모두 배열로 처리
     */
    void deleteActivityAlerts(Long userId, List<Long> alertIds);
} 