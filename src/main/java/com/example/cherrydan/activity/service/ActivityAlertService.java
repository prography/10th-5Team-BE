package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.dto.ActivityAlertResponseDTO;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.activity.strategy.AlertStrategy;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.user.dto.AlertIdsRequestDTO;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityAlertService {
    
    private final ActivityAlertRepository activityAlertRepository;
    private final UserRepository userRepository;
    private final ActivityProcessingService activityProcessingService;
    private final List<AlertStrategy> alertStrategies;

    /**
     * 활동 알림 대상 업데이트 (모든 Strategy 실행)
     */
    @Transactional
    public void updateActivityAlerts() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        alertStrategies.forEach(strategy -> 
            activityProcessingService.processBatchAlertsAsync(strategy, today)
        );
        
        log.info("활동 알림 생성 작업 시작 - {} 개 전략 실행", alertStrategies.size());
        // 메서드 즉시 반환
    }

    /**
     * 활동 알림 발송
     */
    @Transactional
    public void sendActivityNotifications() {
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<ActivityAlert> unnotifiedAlerts = activityAlertRepository.findTodayUnnotifiedAlerts(today);
        
        if (unnotifiedAlerts.isEmpty()) {
            log.info("발송할 활동 알림이 없습니다.");
            return;
        }
        
        // 캠페인별로 그룹핑 (같은 캠페인 = 같은 메시지)
        Map<Campaign, List<ActivityAlert>> groupedByCampaign = unnotifiedAlerts.stream()
                .collect(Collectors.groupingBy(ActivityAlert::getCampaign));
        
        // 캠페인별 병렬 알림 발송 (예외 처리 포함)
        List<CompletableFuture<List<ActivityAlert>>> futures = groupedByCampaign.entrySet().stream()
            .map(entry -> activityProcessingService.sendActivityNotificationAsync(entry.getKey(), entry.getValue())
                .exceptionally(throwable -> {
                    log.error("캠페인 '{}' 알림 발송 실패: {}", entry.getKey().getTitle(), throwable.getMessage());
                    return new ArrayList<>();
                }))
            .toList();
        
        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 결과 수집
        List<ActivityAlert> allAlertsToUpdate = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        int totalSentCount = allAlertsToUpdate.size();
        
        // 성공한 알림들 상태 업데이트
        if (totalSentCount > 0) {
            try {
                allAlertsToUpdate.forEach(ActivityAlert::markAsNotified);
                activityAlertRepository.saveAll(allAlertsToUpdate);
                
                log.info("알림 상태 벌크 업데이트 완료: 성공한 알림 {}개", allAlertsToUpdate.size());
                
            } catch (Exception e) {
                log.error("알림 상태 업데이트 실패: {}", e.getMessage());
            }
        }
        
        log.info("=== 활동 알림 발송 완료: 총 {}건 발송 ===", totalSentCount);
    }

    /**
     * 사용자의 활동 알림 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ActivityAlertResponseDTO> getUserActivityAlerts(Long userId, Pageable pageable) {
        return activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(userId, pageable)
                .map(ActivityAlertResponseDTO::fromEntity);
    }

    /**
     * 사용자의 활동 알림 개수 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Long getUserActivityAlertsCount(Long userId) {
        return activityAlertRepository.countByUserIdAndIsVisibleToUserTrue(userId);
    }


    /**
     * 활동 알림 삭제 (배열)
     */
    @Transactional
    public void deleteActivityAlert(Long userId, List<Long> alertIds) {
        List<ActivityAlert> alerts = activityAlertRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (ActivityAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new UserException(ErrorMessage.ACTIVITY_ALERT_ACCESS_DENIED);
            }
        }
        
        activityAlertRepository.deleteAll(alerts);
        log.info("활동 알림 삭제 완료: userId={}, count={}", userId, alertIds.size());
    }

    /**
     * 활동 알림 읽음 처리 (배열)
     */
    @Transactional
    public void markActivityAlertsAsRead(Long userId, List<Long> alertIds) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        List<ActivityAlert> alerts = activityAlertRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (ActivityAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new UserException(ErrorMessage.ACTIVITY_ALERT_ACCESS_DENIED);
            }
        }
        
        // 읽음 처리
        alerts.forEach(ActivityAlert::markAsRead);
        activityAlertRepository.saveAll(alerts);
        
        log.info("활동 알림 일괄 읽음 처리 완료: userId={}, count={}", userId, alertIds.size());
    }
}