package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityProcessingService {
    
    private final ActivityAlertRepository activityAlertRepository;
    private final NotificationService notificationService;

    /**
     * 캠페인별 활동 알림 생성 및 처리 (비동기)
     */
    @Async("keywordTaskExecutor")
    @Transactional
    public CompletableFuture<List<ActivityAlert>> processCampaignAsync(
            Campaign campaign, List<Bookmark> bookmarks, LocalDate today) {
        
        List<ActivityAlert> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            // 이미 알림이 생성된 사용자들은 제외
            for (Bookmark bookmark : bookmarks) {
                try {
                    // 이미 해당 사용자-캠페인에 대한 알림이 있는지 확인
                    if (activityAlertRepository.existsByUserIdAndCampaignId(
                            bookmark.getUser().getId(), campaign.getId())) {
                        continue; // 이미 알림이 있으면 스킵
                    }
                    
                    // TODO: 추후 필요시 푸시 알림 정책 체크 추가
                    // 현재는 북마크한 모든 사용자에게 알림 생성 (발송 시에 푸시 설정 확인)


                    ActivityAlert alert = ActivityAlert.builder()
                            .user(bookmark.getUser())
                            .campaign(campaign)
                            .alertDate(today)
                            .build();
                    
                    results.add(alert);
                    successCount++;
                    
                } catch (Exception e) {
                    failureCount++;
                    log.error("사용자 {} 캠페인 {} 활동 알림 생성 실패: {}", 
                        bookmark.getUser().getId(), campaign.getId(), e.getMessage());
                }
            }
            
            log.info("캠페인 '{}' 활동 알림 처리 완료: 성공 {}건, 실패 {}건", 
                campaign.getTitle(), successCount, failureCount);
            
            return CompletableFuture.completedFuture(results);
            
        } catch (Exception e) {
            log.error("캠페인 '{}' 활동 알림 처리 중 예외 발생: {}", campaign.getTitle(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 캠페인별 활동 알림 발송 (비동기)
     */
    @Async("keywordTaskExecutor")
    @Transactional
    public CompletableFuture<List<ActivityAlert>> sendActivityNotificationAsync(
            Campaign campaign, List<ActivityAlert> alerts) {
        
        List<ActivityAlert> successAlerts = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            // D-day 계산
            LocalDate today = LocalDate.now();
            int dDay = (int) today.until(campaign.getApplyEnd()).getDays();
            
            // 알림 메시지 구성
            String title = "신청 마감 알림";
            String body = String.format("D-%d %s 신청이 %d일 남았습니다", 
                dDay, campaign.getTitle(), dDay);
            
            NotificationRequest request = NotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(java.util.Map.of(
                            "type", "activity_reminder",
                            "campaign_id", String.valueOf(campaign.getId()),
                            "campaign_title", campaign.getTitle(),
                            "days_remaining", String.valueOf(dDay),
                            "action", "open_activity_page"
                    ))
                    .priority("high")
                    .build();
            
            // 같은 캠페인을 북마크한 사용자들에게 단체 발송
            List<Long> userIds = alerts.stream()
                    .map(alert -> alert.getUser().getId())
                    .collect(Collectors.toList());
            
            NotificationResultDto result = notificationService.sendNotificationToUsers(userIds, request);
            
            // 성공한 사용자들의 알림만 반환
            if (result.getSuccessfulUserIds() != null && !result.getSuccessfulUserIds().isEmpty()) {
                successAlerts = alerts.stream()
                        .filter(alert -> result.getSuccessfulUserIds().contains(alert.getUser().getId()))
                        .collect(Collectors.toList());
                successCount = successAlerts.size();
                
                log.info("활동 알림 발송 성공: 캠페인={}, 성공 사용자 수={}", 
                    campaign.getTitle(), successCount);
            }
            
            failureCount = alerts.size() - successCount;
            
            log.info("캠페인 '{}' 활동 알림 발송 완료: 성공 {}건, 실패 {}건", 
                campaign.getTitle(), successCount, failureCount);
            
            return CompletableFuture.completedFuture(successAlerts);
            
        } catch (Exception e) {
            log.error("캠페인 '{}' 활동 알림 발송 중 예외 발생: {}", campaign.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
}