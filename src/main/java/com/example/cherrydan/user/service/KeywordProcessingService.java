package com.example.cherrydan.user.service;

import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.campaign.service.CampaignServiceImpl;
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

/**
 * 키워드 관련 비동기 처리를 담당하는 서비스
 * AOP 프록시가 정상 작동하도록 별도 서비스로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordProcessingService {
    
    private final KeywordCampaignAlertRepository keywordAlertRepository;
    private final CampaignServiceImpl campaignService;
    private final NotificationService notificationService;

    /**
     * 키워드별 알림 처리 (비동기) - 사용자별 개별 예외 처리
     */
    @Async("keywordTaskExecutor")
    @Transactional
    public CompletableFuture<List<KeywordCampaignAlert>> processKeywordAsync(
            String keyword, List<UserKeyword> userKeywords, LocalDate today) {
        
        List<KeywordCampaignAlert> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            // 지정 날짜 기준으로 생성된 캠페인 수 조회
            long dailyNewCount = campaignService.getDailyCampaignCountByKeyword(keyword, today);
            
            // 신규 캠페인이 없으면 알림 생성하지 않음
            if (dailyNewCount == 0) {
                log.info("키워드 '{}' 어제 신규 캠페인 0개로 알림 생성하지 않음", keyword);
                return CompletableFuture.completedFuture(results);
            }
            
            // 해당 키워드를 등록한 모든 사용자에게 알림 처리
            for (UserKeyword userKeyword : userKeywords) {
                try {
                    KeywordCampaignAlert alert = processKeywordAlert(userKeyword, (int) dailyNewCount, today);
                    if (alert != null) {
                        results.add(alert);
                        successCount++;
                        log.info("키워드 알림 생성: 사용자={}, 키워드={}, 어제신규={}건", 
                                userKeyword.getUser().getId(), keyword, dailyNewCount);
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("키워드 '{}' - 사용자 {} 처리 실패: {}", keyword, userKeyword.getUser().getId(), e.getMessage());
                    // 개별 사용자 실패는 전체 키워드 처리를 중단하지 않음
                }
            }
            
            log.info("키워드 '{}' 처리 완료: 성공 {}건, 실패 {}건", keyword, successCount, failureCount);
            
        } catch (Exception e) {
            // 캠페인 수 조회 실패 등 키워드 전체 실패
            log.error("키워드 '{}' 전체 처리 실패: {}", keyword, e.getMessage());
            throw e; // 이 경우에만 exceptionally로 전파
        }
        
        return CompletableFuture.completedFuture(results);
    }

    /**
     * 키워드별 단체 알림 발송 (비동기)
     */
    @Async("keywordTaskExecutor")
    @Transactional
    public CompletableFuture<List<KeywordCampaignAlert>> sendKeywordNotificationAsync(
            String keyword, List<KeywordCampaignAlert> alerts) {
        
        List<KeywordCampaignAlert> successfulAlerts = new ArrayList<>();
        
        try {
            // 간단! campaignCount가 어제 신규 증가분
            int dailyNewCount = alerts.get(0).getCampaignCount();
            
            String title = "체리단";
            String countText;
            
            // 신규 증가분에 따른 표시 방식
            if (dailyNewCount >= 100) {
                countText = "+100";  // 100+건
            } else if (dailyNewCount >= 10) {
                countText = "+10";   // 10+건  
            } else {
                countText = String.valueOf(dailyNewCount); // 정확한 수 (1~9건)
            }
            
            String body = String.format("'%s' 키워드 캠페인이 %s건 등록됐어요. \n지금 체리단에서 확인해 보세요.", 
                    keyword, countText);
            
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(java.util.Map.of(
                            "type", "keyword_campaign",
                            "keyword", keyword,
                            "dailyNewCount", String.valueOf(dailyNewCount),
                            "action", "open_personalized_page"
                    ))
                    .priority("high")
                    .build();
            
            // 같은 키워드를 가진 사용자들에게 단체 발송
            List<Long> userIds = alerts.stream()
                    .map(alert -> alert.getUser().getId())
                    .collect(Collectors.toList());
            
            NotificationResultDto result = notificationService.sendNotificationToUsers(userIds, notificationRequest);
            
            // 성공한 사용자들의 알림만 반환
            if (result.getSuccessfulUserIds() != null && !result.getSuccessfulUserIds().isEmpty()) {
                successfulAlerts = alerts.stream()
                        .filter(alert -> result.getSuccessfulUserIds().contains(alert.getUser().getId()))
                        .collect(Collectors.toList());
                
                log.info("키워드별 단체 발송 성공: 키워드={}, 어제신규={}건, 대상사용자={}명, 성공={}명", 
                        keyword, dailyNewCount, userIds.size(), successfulAlerts.size());
            } else {
                log.warn("키워드별 단체 발송 완전 실패: 키워드={}, 대상사용자={}명", keyword, userIds.size());
            }
            
        } catch (Exception e) {
            log.error("키워드별 단체 발송 중 오류: 키워드={}, 오류={}", keyword, e.getMessage());
            throw e; // exceptionally에서 처리하도록 예외 전파
        }
        
        return CompletableFuture.completedFuture(successfulAlerts);
    }

    /**
     * 개별 사용자-키워드 조합에 대한 알림 처리 (간단 버전)
     * 사용자의 푸시 설정만 확인하고 신규 증가분이 있으면 알림 생성
     */
    private KeywordCampaignAlert processKeywordAlert(UserKeyword userKeyword, int dailyNewCount, LocalDate today) {
        // 1. 사용자 푸시 설정 확인
        // User user = userKeyword.getUser();

        // if (user.getPushSettings() == null || 
        //     !user.getPushSettings().getPushEnabled() || 
        //     !user.getPushSettings().getPersonalizedEnabled()) {
        //     log.info("사용자 푸시 설정으로 인해 키워드 알림 생성 제외: 사용자={}, 키워드={}", 
        //             user.getId(), userKeyword.getKeyword());
        //     return null;
        // }
        
        // 2. 어제 신규 증가분이 있으므로 알림 생성 (간단!)
        return createKeywordAlertEntity(userKeyword, dailyNewCount, today);
    }


    
    /**
     * 키워드 알림 엔티티 생성 (어제 신규 증가분 기준)
     */
    private KeywordCampaignAlert createKeywordAlertEntity(UserKeyword userKeyword, int dailyNewCount, LocalDate today) {
        // 어제 신규 증가분으로 알림 생성 (간단!)
        return KeywordCampaignAlert.builder()
                .user(userKeyword.getUser())
                .keyword(userKeyword.getKeyword())
                .campaignCount(dailyNewCount) // 어제 신규 증가분 저장
                .alertDate(today)
                .alertStage(0) // 발송 대기 상태
                .isVisibleToUser(true)
                .build();
    }
}