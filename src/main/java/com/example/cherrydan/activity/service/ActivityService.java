package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.ActivityCampaignResponseDTO;
import java.util.List;

public interface ActivityService {
    
    /**
     * 사용자의 활동 캠페인 목록 조회 (3일 이내 마감)
     */
    List<ActivityCampaignResponseDTO> getActivityCampaigns(Long userId);
    
    /**
     * 활동 알림 발송
     */
    void sendActivityNotifications();

    /**
     * 활동 알림(캠페인 상태) 개별 삭제 (isActive = false)
     */
    void deleteActivityAlert(Long userId, Long alertId);
} 