package com.example.cherrydan.user.service;

import com.example.cherrydan.campaign.service.CampaignServiceImpl;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.user.repository.UserKeywordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class UserKeywordServiceAsyncTest {

    private static final Logger log = LoggerFactory.getLogger(UserKeywordServiceAsyncTest.class);

    @Autowired
    private UserKeywordService userKeywordService;
    
    @Autowired
    private KeywordProcessingService keywordProcessingService;
    
    @Autowired
    private UserKeywordRepository userKeywordRepository;
    
    @Autowired
    private KeywordCampaignAlertRepository alertRepository;
    
    @Autowired
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("키워드별 비동기 알림 생성 테스트")
    void testProcessKeywordAsync() throws Exception {
        // Given
        log.info("=== 키워드별 비동기 알림 생성 테스트 시작 ===");
        
        // 기존 알림 데이터 정리
        alertRepository.deleteAll();
        
        // 사용자별 키워드 조회
        List<UserKeyword> allKeywords = userKeywordRepository.findAllWithUser();
        log.info("전체 키워드 개수: {}", allKeywords.size());
        
        // 키워드별 그룹핑
        Map<String, List<UserKeyword>> keywordGroups = allKeywords.stream()
                .collect(Collectors.groupingBy(UserKeyword::getKeyword));
        
        log.info("유니크 키워드 개수: {}", keywordGroups.size());
        
        // When - 키워드별 비동기 처리
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<List<KeywordCampaignAlert>>> futures = keywordGroups.entrySet().stream()
                .map(entry -> {
                    log.info("키워드 '{}' 비동기 처리 시작, 사용자 수: {}", 
                            entry.getKey(), entry.getValue().size());
                    
                    return keywordProcessingService.processKeywordAsync(
                            entry.getKey(), 
                            entry.getValue(), 
                            LocalDate.of(2025, 7, 21) // 7월 21일로 고정 (어제: 7월 20일 데이터 조회)
                    ).exceptionally(throwable -> {
                        log.error("키워드 '{}' 처리 실패: {}", entry.getKey(), throwable.getMessage());
                        return List.of();
                    });
                })
                .collect(Collectors.toList());
        
        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        log.info("비동기 처리 완료 시간: {}ms", endTime - startTime);
        
        // 결과 수집
        List<KeywordCampaignAlert> allAlerts = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        // Then
        log.info("생성된 알림 개수: {}", allAlerts.size());
        
        assertThat(allAlerts).isNotEmpty();
        
        // 생성된 알림 상세 정보 로깅
        allAlerts.forEach(alert -> {
            log.info("생성된 알림 - 사용자: {}, 키워드: '{}', 캠페인수: {}, 단계: {}", 
                    alert.getUser().getId(), 
                    alert.getKeyword(), 
                    alert.getCampaignCount(),
                    alert.getAlertStage());
        });
        
        // 키워드별 통계
        Map<String, Long> keywordStats = allAlerts.stream()
                .collect(Collectors.groupingBy(
                        KeywordCampaignAlert::getKeyword,
                        Collectors.counting()
                ));
        
        log.info("키워드별 생성된 알림 통계:");
        keywordStats.forEach((keyword, count) -> 
                log.info("  - '{}': {}개 알림", keyword, count));
        
        log.info("=== 키워드별 비동기 알림 생성 테스트 완료 ===");
    }
    
    @Test
    @DisplayName("전체 키워드 알림 배치 업데이트 테스트")
    void testUpdateKeywordCampaignAlerts() {
        // Given
        log.info("=== 전체 키워드 알림 배치 업데이트 테스트 시작 ===");
        
        // 기존 알림 정리
        long beforeCount = alertRepository.count();
        log.info("테스트 시작 전 기존 알림 개수: {}", beforeCount);
        
        // When
        long startTime = System.currentTimeMillis();
        userKeywordService.updateKeywordCampaignAlerts();
        long endTime = System.currentTimeMillis();
        
        // Then
        long afterCount = alertRepository.count();
        log.info("테스트 완료 후 총 알림 개수: {}", afterCount);
        log.info("새로 생성된 알림 개수: {}", afterCount - beforeCount);
        log.info("전체 배치 처리 시간: {}ms", endTime - startTime);
        
        // 생성된 알림들 확인
        List<KeywordCampaignAlert> newAlerts = alertRepository.findAll();
        log.info("생성된 알림 상세:");
        newAlerts.stream()
                .limit(5) // 처음 5개만 로깅
                .forEach(alert -> {
                    log.info("  알림 ID: {}, 사용자: {}, 키워드: '{}', 캠페인수: {}, 단계: {}", 
                            alert.getId(),
                            alert.getUser().getId(), 
                            alert.getKeyword(),
                            alert.getCampaignCount(),
                            alert.getAlertStage());
                });
        
        assertThat(afterCount).isGreaterThanOrEqualTo(beforeCount);
        log.info("=== 전체 키워드 알림 배치 업데이트 테스트 완료 ===");
    }
    
    @Test
    @DisplayName("키워드별 캠페인 수 조회 성능 테스트")
    void testCampaignCountPerformance() {
        // Given
        List<String> testKeywords = List.of("맛집", "카페", "여행", "뷰티", "패션");
        
        log.info("=== 키워드별 캠페인 수 조회 성능 테스트 시작 ===");
        
        // When & Then
        testKeywords.forEach(keyword -> {
            long startTime = System.currentTimeMillis();
            long campaignCount = campaignService.getDailyCampaignCountByKeyword(keyword, LocalDate.of(2025, 7, 20)); // 어제 날짜로 고정
            long endTime = System.currentTimeMillis();
            
            log.info("키워드 '{}': {}개 캠페인, 조회 시간: {}ms", 
                    keyword, campaignCount, endTime - startTime);
        });
        
        log.info("=== 키워드별 캠페인 수 조회 성능 테스트 완료 ===");
    }
    
    @Test
    @DisplayName("사용자 푸시 설정 확인 테스트")
    void testUserPushSettings() {
        // Given
        List<UserKeyword> userKeywords = userKeywordRepository.findAllWithUser();
        
        log.info("=== 사용자 푸시 설정 확인 테스트 시작 ===");
        log.info("전체 사용자-키워드 조합 수: {}", userKeywords.size());
        
        // When & Then
        Map<Long, List<UserKeyword>> userGroups = userKeywords.stream()
                .collect(Collectors.groupingBy(uk -> uk.getUser().getId()));
        
        userGroups.forEach((userId, keywords) -> {
            User user = keywords.get(0).getUser();
            boolean canReceiveAlert = user.getPushSettings() != null && 
                    user.getPushSettings().getPushEnabled() && 
                    user.getPushSettings().getPersonalizedEnabled();
            
            log.info("사용자 {}: 키워드 {}개, 알림 수신 가능: {}", 
                    userId, keywords.size(), canReceiveAlert);
            
            if (user.getPushSettings() != null) {
                log.info("  - 푸시 활성화: {}, 맞춤 알림: {}, 마케팅 알림: {}", 
                        user.getPushSettings().getPushEnabled(),
                        user.getPushSettings().getPersonalizedEnabled(),
                        user.getPushSettings().getMarketingEnabled());
            } else {
                log.info("  - 푸시 설정이 없습니다.");
            }
        });
        
        long eligibleUsers = userGroups.values().stream()
                .mapToLong(keywords -> {
                    User user = keywords.get(0).getUser();
                    return (user.getPushSettings() != null && 
                            user.getPushSettings().getPushEnabled() && 
                            user.getPushSettings().getPersonalizedEnabled()) ? 1 : 0;
                })
                .sum();
        
        log.info("알림 수신 가능한 사용자 수: {} / {}", eligibleUsers, userGroups.size());
        log.info("=== 사용자 푸시 설정 확인 테스트 완료 ===");
    }
}