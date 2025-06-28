package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.ActivityCampaignResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<ActivityCampaignResponseDTO> getActivityCampaigns(Long userId) {
        // 사용자의 활동 알림 내역 중 isVisibleToUser=true인 것만 조회
        List<CampaignStatus> activityStatuses = campaignStatusRepository
                .findVisibleActivityByUserId(userId);
        return activityStatuses.stream()
                .filter(CampaignStatus::isActivityEligible) // 3일 이내 마감인 것만 필터링
                .map(ActivityCampaignResponseDTO::fromEntity)
                .collect(Collectors.toList());
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
                        .androidIcon("ic_activity")
                        .androidColor("#FF6B6B")
                        .androidSound("default")
                        .iosSound("default")
                        .iosBadge(1)
                        .iosCategory("ACTIVITY")
                        .ttl(86400L) // 24시간
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
    public void deleteActivityAlert(Long userId, Long alertId) {
        CampaignStatus alert = campaignStatusRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        if (!alert.getUser().getId().equals(userId)) {
            throw new SecurityException("본인 알림만 삭제할 수 있습니다.");
        }
        alert.setIsVisibleToUser(false); // 사용자에게만 숨김
        campaignStatusRepository.save(alert);
    }
} 