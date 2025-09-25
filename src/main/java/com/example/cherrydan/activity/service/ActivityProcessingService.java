package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.strategy.AlertStrategy;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityProcessingService {
    
    private final ActivityAlertRepository activityAlertRepository;
    private final NotificationService notificationService;
    
    private static final int BATCH_SIZE = 500;

    /**
     * 배치 처리 방식으로 알림 생성
     */
    @Async("alertTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processBatchAlertsAsync(
            AlertStrategy strategy, LocalDate today) {

        String strategyName = strategy.getClass().getSimpleName();
        List<ActivityAlert> batch = new ArrayList<>(BATCH_SIZE);
        int totalProcessed = 0;
        int totalSkipped = 0;
        long startTime = System.currentTimeMillis();

        try {
            log.info("[{}] 배치 처리 시작", strategyName);

            // Iterator 패턴으로 스트리밍 처리
            Iterator<ActivityAlert> iterator = strategy.generateAlertsIterator(today);

            while (iterator.hasNext()) {
                batch.add(iterator.next());

                if (batch.size() >= BATCH_SIZE) {
                    BatchResult result = saveBatchWithDuplicateHandling(batch);
                    totalProcessed += result.processed();
                    totalSkipped += result.skipped();
                    batch.clear();

                    log.debug("[{}] 배치 저장: {} 건 처리, {} 건 스킵",
                        strategyName, result.processed(), result.skipped());
                }
            }

            // 남은 배치 처리
            if (!batch.isEmpty()) {
                BatchResult result = saveBatchWithDuplicateHandling(batch);
                totalProcessed += result.processed();
                totalSkipped += result.skipped();
            }

            long elapsed = System.currentTimeMillis() - startTime;

            log.info("[{}] 완료: {} 건 처리, {} 건 중복 스킵 (소요시간: {}ms)",
                strategyName, totalProcessed, totalSkipped, elapsed);

        } catch (Exception e) {
            log.error("[{}] 실패: {}", strategyName, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }
    
    private record BatchResult(int processed, int skipped) {}
    
    private BatchResult saveBatchWithDuplicateHandling(List<ActivityAlert> batch) {
        int processed = 0;
        int skipped = 0;
        
        try {
            activityAlertRepository.saveAllAndFlush(batch);
            processed = batch.size();
            
        } catch (DataIntegrityViolationException e) {
            // 중복 발생 시 개별 저장으로 폴백
            log.debug("중복 감지, 개별 저장 모드로 전환");
            
            for (ActivityAlert alert : batch) {
                try {
                    activityAlertRepository.save(alert);
                    processed++;
                } catch (DataIntegrityViolationException ignored) {
                    // DB unique constraint가 중복 방지
                    skipped++;
                }
            }
        }
        
        return new BatchResult(processed, skipped);
    }

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
        
        try {
            // 알림 타입별로 그룹핑 (같은 타입 = 같은 메시지)
            for (ActivityAlert alert : alerts) {
                try {
                    // ActivityAlert에서 title과 body 가져오기
                    String title = alert.getNotificationTitle();
                    String body = alert.getNotificationBody();
                    
                    NotificationRequest request = NotificationRequest.builder()
                            .title(title)
                            .body(body)
                            .data(java.util.Map.of(
                                    "type", "activity_alert",
                                    "alert_type", alert.getAlertType().name(),
                                    "campaign_id", String.valueOf(campaign.getId()),
                                    "campaign_title", campaign.getTitle(),
                                    "action", "open_activity_page"
                            ))
                            .priority("high")
                            .build();
                    
                    // 개별 발송 (사용자마다 다른 메시지일 수 있음)
                    NotificationResultDto result = notificationService.sendNotificationToUsers(
                        List.of(alert.getUser().getId()), request);
                    
                    if (result.getSuccessCount() > 0) {
                        successAlerts.add(alert);
                        log.debug("알림 발송 성공: userId={}, alertType={}, campaign={}", 
                            alert.getUser().getId(), alert.getAlertType(), campaign.getTitle());
                    }
                } catch (Exception e) {
                    log.error("알림 발송 실패: userId={}, alertId={}", 
                        alert.getUser().getId(), alert.getId(), e);
                }
            }
            
            log.info("캠페인 '{}' 활동 알림 발송 완료: 성공 {}건, 실패 {}건", 
                campaign.getTitle(), successAlerts.size(), alerts.size() - successAlerts.size());
            
            return CompletableFuture.completedFuture(successAlerts);
            
        } catch (Exception e) {
            log.error("캠페인 '{}' 활동 알림 발송 중 예외 발생: {}", campaign.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
}