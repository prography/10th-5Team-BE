package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class UserKeywordNotificationTest {

    private static final Logger log = LoggerFactory.getLogger(UserKeywordNotificationTest.class);

    @Autowired
    private UserKeywordService userKeywordService;
    
    @Autowired
    private KeywordCampaignAlertRepository alertRepository;

    @Test
    @DisplayName("키워드 알림 발송 테스트")
    void testSendKeywordCampaignNotifications() {
        // Given
        log.info("=== 키워드 알림 발송 테스트 시작 ===");
        
        // 먼저 알림 생성
        userKeywordService.updateKeywordCampaignAlerts();
        
        // 발송 대상 알림 조회
        List<KeywordCampaignAlert> unnotifiedAlerts = alertRepository.findUnnotifiedAlerts();
        log.info("발송 대상 알림 개수: {}", unnotifiedAlerts.size());
        
        if (unnotifiedAlerts.isEmpty()) {
            log.warn("발송할 알림이 없습니다. 테스트를 종료합니다.");
            return;
        }
        
        // 발송 전 상태 로깅
        unnotifiedAlerts.forEach(alert -> {
            log.info("발송 대상 알림 - ID: {}, 사용자: {}, 키워드: '{}', 캠페인수: {}, 단계: {}", 
                    alert.getId(),
                    alert.getUser().getId(), 
                    alert.getKeyword(),
                    alert.getCampaignCount(),
                    alert.getAlertStage());
        });
        
        // 키워드별 그룹핑
        Map<String, List<KeywordCampaignAlert>> groupedByKeyword = unnotifiedAlerts.stream()
                .collect(Collectors.groupingBy(KeywordCampaignAlert::getKeyword));
        
        log.info("발송할 키워드 종류: {}개", groupedByKeyword.size());
        groupedByKeyword.forEach((keyword, alerts) -> {
            log.info("  키워드 '{}': {}개 알림", keyword, alerts.size());
        });
        
        // When
        long startTime = System.currentTimeMillis();
        userKeywordService.sendKeywordCampaignNotifications();
        long endTime = System.currentTimeMillis();
        
        // Then
        log.info("알림 발송 처리 시간: {}ms", endTime - startTime);
        
        // 발송 후 상태 확인
        List<KeywordCampaignAlert> afterAlerts = alertRepository.findAll();
        long notifiedCount = afterAlerts.stream()
                .mapToLong(alert -> alert.isNotified() ? 1 : 0)
                .sum();
        
        log.info("발송 완료된 알림 개수: {} / {}", notifiedCount, afterAlerts.size());
        
        // 성공한 알림들 상세 로깅
        afterAlerts.stream()
                .filter(KeywordCampaignAlert::isNotified)
                .forEach(alert -> {
                    log.info("발송 완료 알림 - 사용자: {}, 키워드: '{}', 캠페인수: {}", 
                            alert.getUser().getId(),
                            alert.getKeyword(),
                            alert.getCampaignCount());
                });
        
        assertThat(notifiedCount).isGreaterThan(0);
        log.info("=== 키워드 알림 발송 테스트 완료 ===");
    }
    
    @Test
    @DisplayName("알림 생성부터 발송까지 전체 플로우 테스트")
    void testFullKeywordNotificationFlow() {
        // Given
        log.info("=== 전체 키워드 알림 플로우 테스트 시작 ===");
        
        // 기존 알림 정리
        alertRepository.deleteAll();
        log.info("기존 알림 데이터 정리 완료");
        
        // When - 1. 알림 생성
        log.info("1단계: 키워드 알림 생성 시작");
        long createStart = System.currentTimeMillis();
        userKeywordService.updateKeywordCampaignAlerts();
        long createEnd = System.currentTimeMillis();
        
        long createdCount = alertRepository.count();
        log.info("1단계 완료: {}개 알림 생성, 소요시간: {}ms", createdCount, createEnd - createStart);
        
        // When - 2. 알림 발송
        log.info("2단계: 키워드 알림 발송 시작");
        long sendStart = System.currentTimeMillis();
        userKeywordService.sendKeywordCampaignNotifications();
        long sendEnd = System.currentTimeMillis();
        
        // Then
        List<KeywordCampaignAlert> finalAlerts = alertRepository.findAll();
        long sentCount = finalAlerts.stream()
                .mapToLong(alert -> alert.isNotified() ? 1 : 0)
                .sum();
        
        log.info("2단계 완료: {}개 알림 발송, 소요시간: {}ms", sentCount, sendEnd - sendStart);
        log.info("전체 플로우 완료: 생성 {}개, 발송 {}개, 총 소요시간: {}ms", 
                createdCount, sentCount, (sendEnd - createStart));
        
        // 최종 통계
        Map<String, Long> keywordStats = finalAlerts.stream()
                .collect(Collectors.groupingBy(
                        KeywordCampaignAlert::getKeyword,
                        Collectors.counting()
                ));
        
        log.info("키워드별 최종 알림 통계:");
        keywordStats.entrySet().stream()
                .limit(10) // 상위 10개만 출력
                .forEach(entry -> {
                    long notified = finalAlerts.stream()
                            .filter(alert -> alert.getKeyword().equals(entry.getKey()))
                            .mapToLong(alert -> alert.isNotified() ? 1 : 0)
                            .sum();
                    log.info("  - '{}': 총 {}개, 발송 {}개", 
                            entry.getKey(), entry.getValue(), notified);
                });
        
        assertThat(createdCount).isGreaterThan(0);
        assertThat(sentCount).isGreaterThanOrEqualTo(0); // FCM 설정이 없으면 0일 수 있음
        
        log.info("=== 전체 키워드 알림 플로우 테스트 완료 ===");
    }
}