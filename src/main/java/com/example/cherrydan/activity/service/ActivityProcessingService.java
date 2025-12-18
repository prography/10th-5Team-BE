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
}