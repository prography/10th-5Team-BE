package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.ActivityNotificationResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    
    private final CampaignStatusRepository campaignStatusRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityNotificationResponseDTO> getActivityNotifications(Long userId, Pageable pageable) {
        // 사용자의 활동 알림 목록 조회 (페이지네이션)
        // - isVisibleToUser=true인 것만 조회 (삭제되지 않은 것)
        // - 읽음/안읽음 상관없이 모두 보여줌 (isRead 필드로 구분)
        Page<CampaignStatus> activityStatusesPage = campaignStatusRepository
                .findVisibleActivityByUserId(userId, pageable);
        
        return activityStatusesPage.map(status -> {
            if (status.isActivityEligible()) {
                return ActivityNotificationResponseDTO.fromEntity(status);
            } else {
                return null; // 3일 이내 마감이 아닌 경우 null 반환
            }
        });
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(Long userId, List<Long> campaignStatusIds) {
        List<CampaignStatus> campaignStatuses = campaignStatusRepository.findAllById(campaignStatusIds);
        
        // 모든 캠페인 상태가 해당 사용자의 것인지 확인
        for (CampaignStatus campaignStatus : campaignStatuses) {
            if (!campaignStatus.getUser().getId().equals(userId)) {
                throw new SecurityException("본인의 알림만 읽음 처리할 수 있습니다.");
            }
        }
        
        // 읽음 처리
        campaignStatuses.forEach(CampaignStatus::markAsRead);
        campaignStatusRepository.saveAll(campaignStatuses);
        
        log.info("활동 알림 읽음 처리 완료: userId={}, count={}", userId, campaignStatusIds.size());
    }

    @Override
    @Transactional
    public void sendActivityNotifications() {
        log.info("활동 알림 발송 시작");
        
        // 알림 미발송된 활동 대상 캠페인들 조회
        List<CampaignStatus> unnotifiedStatuses = campaignStatusRepository
                .findUnnotifiedActivityStatuses();
        
        // 사용자별로 그룹핑
        Map<Long, List<CampaignStatus>> groupedByUser = unnotifiedStatuses.stream()
                .filter(CampaignStatus::isActivityEligible) // 3일 이내 마감인 것만
                .collect(Collectors.groupingBy(status -> status.getUser().getId()));
        
        for (Map.Entry<Long, List<CampaignStatus>> entry : groupedByUser.entrySet()) {
            Long userId = entry.getKey();
            List<CampaignStatus> userStatuses = entry.getValue();
            
            try {
                // 사용자의 푸시 설정 확인
                if (!isActivityNotificationEnabled(userId)) {
                    log.info("사용자 {}의 활동 알림이 비활성화되어 있음", userId);
                    continue;
                }
                
                // 가장 급한 캠페인으로 대표 알림 생성
                CampaignStatus urgentStatus = userStatuses.stream()
                        .min((s1, s2) -> Integer.compare(s1.getDaysRemaining(), s2.getDaysRemaining()))
                        .orElse(userStatuses.get(0));
                
                String title = "활동 마감 알림";
                String body = String.format("%s 활동이 %d일 남았습니다. 놓치지 마세요.", 
                        urgentStatus.getCampaign().getTitle(), urgentStatus.getDaysRemaining());
                
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .title(title)
                        .body(body)
                        .data(java.util.Map.of(
                                "type", "activity_reminder",
                                "campaign_id", String.valueOf(urgentStatus.getCampaign().getId()),
                                "days_remaining", String.valueOf(urgentStatus.getDaysRemaining()),
                                "action", "open_activity_page"
                        ))
                        .priority("high")
                        .build();
                
                notificationService.sendNotificationToUser(userId, notificationRequest);
                
                // 알림 발송 완료 표시
                userStatuses.forEach(CampaignStatus::markActivityAsNotified);
                
                log.info("활동 알림 발송 완료: 사용자={}, 활동수={}", userId, userStatuses.size());
                
            } catch (Exception e) {
                log.error("활동 알림 발송 실패: 사용자={}, 오류={}", userId, e.getMessage());
            }
        }
        
        log.info("활동 알림 발송 완료");
    }
    
    /**
     * 사용자의 활동 알림 활성화 여부 확인
     * 푸시 설정과 활동 알림 설정이 모두 활성화되어 있어야 함
     */
    private boolean isActivityNotificationEnabled(Long userId) {
        User user = userRepository.findActiveById(userId).orElse(null);
        return user != null && 
               user.getPushSettings() != null && 
               user.getPushSettings().getPushEnabled() && 
               user.getPushSettings().getActivityEnabled();
    }

    @Override
    @Transactional
    public void deleteActivityAlerts(Long userId, List<Long> alertIds) {
        List<CampaignStatus> alerts = campaignStatusRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (CampaignStatus alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new SecurityException("본인 알림만 삭제할 수 있습니다.");
            }
        }
        
        // 사용자에게만 숨김 처리
        alerts.forEach(alert -> alert.setIsVisibleToUser(false));
        campaignStatusRepository.saveAll(alerts);
        
        log.info("활동 알림 삭제 완료: userId={}, count={}", userId, alertIds.size());
    }
} 