package com.example.cherrydan.notification.scheduler;

import com.example.cherrydan.activity.service.ActivityAlertService;
import com.example.cherrydan.user.service.UserKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 통합 알림 스케줄러
 * 매일 오전 10시에 활동 알림과 키워드 맞춤 알림을 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    
    private final ActivityAlertService activityAlertService;
    private final UserKeywordService userKeywordService;
    
    
    /**
     * 10분마다 실행 - 키워드 맞춤 알림 발송 (테스트용)
     */
    @Scheduled(cron = "0 0 11 * * ?", zone = "Asia/Seoul")
    public void sendDailyKeywordNotifications() {
        log.info("=== 일일 키워드 맞춤 알림 발송 작업 시작 ===");
        
        try {
            // 키워드 맞춤 알림 발송 (새벽 5시에 업데이트된 데이터 기반)
            userKeywordService.sendKeywordCampaignNotifications();
            
            log.info("=== 일일 키워드 맞춤 알림 발송 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("일일 키워드 맞춤 알림 발송 작업 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 10분마다 실행 - 키워드 맞춤 알림 대상 업데이트 (테스트용, 알림 발송보다 먼저)
     */
    @Scheduled(cron = "0 30 7 * * ?", zone = "Asia/Seoul")
    public void updateKeywordCampaignAlerts() {
        log.info("=== 새벽 키워드 알림 업데이트 작업 시작 ===");
        
        try {
            // 키워드 맞춤 알림 대상 업데이트 (10개 이상인 키워드만)
            userKeywordService.updateKeywordCampaignAlerts();
            
            log.info("=== 새벽 키워드 알림 업데이트 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("새벽 키워드 알림 업데이트 작업 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 10분마다 실행 - 활동 알림 대상 업데이트 (테스트용, 알림 발송보다 먼저)
     */
    @Scheduled(cron = "0 0 6 * * ?", zone = "Asia/Seoul")
    public void updateActivityAlerts() {
        log.info("=== 활동 알림 업데이트 작업 시작 ===");
        
        try {
            activityAlertService.updateActivityAlerts();
            
            log.info("=== 활동 알림 업데이트 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("활동 알림 업데이트 작업 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 10분마다 실행 - 활동 알림 발송 (테스트용)
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul")
    public void sendActivityNotifications() {
        log.info("=== 활동 알림 발송 작업 시작 ===");
        
        try {
            activityAlertService.sendActivityNotifications();
            
            log.info("=== 활동 알림 발송 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("활동 알림 발송 작업 실패: {}", e.getMessage(), e);
        }
    }
} 