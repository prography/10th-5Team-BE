package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.dto.ActivityAlertResponseDTO;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.activity.strategy.AlertStrategy;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.dto.AlertIdsRequestDTO;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityAlertService {

    private final ActivityAlertRepository activityAlertRepository;
    private final UserRepository userRepository;
    private final ActivityProcessingService activityProcessingService;
    private final List<AlertStrategy> alertStrategies;
    private final NotificationService notificationService;

    private static final int BATCH_SIZE = 500;

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

        // 페이징 설정
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        Page<ActivityAlert> page;
        int totalSentCount = 0;
        int totalProcessed = 0;

        log.info("=== 활동 알림 발송 시작 ===");

        // 페이지 단위로 처리
        do {
            page = activityAlertRepository.findTodayUnnotifiedAlertsWithPaging(today, pageable);

            if (page.isEmpty()) {
                log.info("발송할 활동 알림이 없습니다.");
                break;
            }

            // 배치 처리 및 즉시 상태 업데이트
            int batchSentCount = processBatchNotifications(page.getContent());
            totalSentCount += batchSentCount;
            totalProcessed += page.getNumberOfElements();

            log.info("배치 처리 완료: {} / {} 건 발송 성공", batchSentCount, page.getNumberOfElements());

            // 다음 페이지로 이동
            pageable = page.nextPageable();

        } while (page.hasNext());

        log.info("=== 활동 알림 발송 완료: 총 {} / {} 건 발송 ===", totalSentCount, totalProcessed);
    }

    /**
     * 배치 단위 알림 발송 및 상태 업데이트
     * @return 성공적으로 발송된 알림 개수
     */
    private int processBatchNotifications(List<ActivityAlert> batch) {
        ArrayList<ActivityAlert> successfulAlerts = new ArrayList<>();

        for (ActivityAlert alert : batch) {
            try {
                // 개별 알림 발송 (ActivityAlert가 이미 모든 정보를 가지고 있음)
                NotificationRequest request = NotificationRequest.builder()
                    .title(alert.getNotificationTitle())
                    .body(alert.getNotificationBody())
                    .data(Map.of(
                        "type", "activity_alert",
                        "alert_type", alert.getAlertType().name(),
                        "campaign_id", String.valueOf(alert.getCampaign().getId()),
                        "campaign_title", alert.getCampaign().getTitle(),
                        "action", "open_activity_page"
                    ))
                    .priority("high")
                    .build();

                // 사용자에게 발송
                NotificationResultDto result = notificationService.sendNotificationToUsers(
                    List.of(alert.getUser().getId()), request);

                if (result.getSuccessCount() > 0) {
                    // 성공 시 즉시 상태 업데이트
                    alert.markAsNotified();
                    successfulAlerts.add(alert);

                    log.debug("알림 발송 성공: userId={}, alertType={}, campaignId={}",
                        alert.getUser().getId(), alert.getAlertType(), alert.getCampaign().getId());
                } else {
                    log.debug("알림 발송 실패: userId={}, alertType={}, campaignId={}",
                        alert.getUser().getId(), alert.getAlertType(), alert.getCampaign().getId());
                }

            } catch (Exception e) {
                log.error("알림 발송 중 오류: userId={}, alertId={}, error={}",
                    alert.getUser().getId(), alert.getId(), e.getMessage());
            }
        }

        if (!successfulAlerts.isEmpty()){
            activityAlertRepository.saveAll(successfulAlerts);
        }

        return successfulAlerts.size();
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
     * 활동 알림 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteActivityAlert(Long userId, List<Long> alertIds) {
        List<ActivityAlert> alerts = activityAlertRepository.findAllById(alertIds);

        // 모든 알림이 해당 사용자의 것인지 확인
        for (ActivityAlert alert : alerts) {
            User user = alert.getUser();
            if (user == null || !user.getId().equals(userId)) {
                throw new UserException(ErrorMessage.ACTIVITY_ALERT_ACCESS_DENIED);
            }
            alert.hide();
        }

        activityAlertRepository.saveAll(alerts);
        log.info("활동 알림 숨김 처리 완료: userId={}, count={}", userId, alertIds.size());
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