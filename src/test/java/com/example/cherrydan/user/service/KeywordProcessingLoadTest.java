package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.domain.UserPushSettings;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.user.repository.UserKeywordRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.campaign.service.CampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("키워드 처리 부하 테스트")
class KeywordProcessingLoadTest {

    private static final Logger log = LoggerFactory.getLogger(KeywordProcessingLoadTest.class);
    
    private static final int KEYWORD_COUNT = 1000;
    private static final int USER_COUNT = 100;
    private static final int KEYWORDS_PER_USER = 10; // 사용자당 등록할 키워드 수
    
    @Autowired
    private KeywordProcessingService keywordProcessingService;
    
    @Autowired
    private UserKeywordService userKeywordService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserKeywordRepository userKeywordRepository;
    
    @Autowired
    private KeywordCampaignAlertRepository alertRepository;
    
    @Autowired
    private CampaignServiceImpl campaignService;

    private List<User> testUsers;
    private List<String> testKeywords;

    @BeforeEach
    @Transactional
    void setUp() {
        log.info("=== 부하테스트 데이터 초기화 시작 ===");
        
        // 기존 테스트 데이터 정리
        alertRepository.deleteAll();
        userKeywordRepository.deleteAll();
        
        // 테스트 사용자 생성 (100명)
        testUsers = createTestUsers(USER_COUNT);
        log.info("테스트 사용자 {}명 생성 완료", testUsers.size());
        
        // 테스트 키워드 생성 (1000개)
        testKeywords = generateTestKeywords(KEYWORD_COUNT);
        log.info("테스트 키워드 {}개 생성 완료", testKeywords.size());
        
        // 사용자-키워드 매핑 생성 (사용자당 10개 키워드, 랜덤 분배)
        createUserKeywordMappings();
        
        long totalMappings = userKeywordRepository.count();
        log.info("사용자-키워드 매핑 {}개 생성 완료", totalMappings);
        
        log.info("=== 부하테스트 데이터 초기화 완료 ===");
    }

    @Test
    @DisplayName("실제 스케줄러 시나리오 - 1000개 키워드 × 100명 사용자 처리 성능 테스트")
    void scheduleRealWorldTest() throws Exception {
        log.info("=== 실제 스케줄러 시나리오 부하테스트 시작 ===");
        log.info("키워드 수: {}, 사용자 수: {}, 총 매핑: {}", 
                KEYWORD_COUNT, USER_COUNT, userKeywordRepository.count());
        
        // 메모리 사용량 측정 시작
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // 성능 측정 시작  
        long startTime = System.currentTimeMillis();
        
        // 실제 스케줄러 메서드 호출 (단일 호출)
        userKeywordService.updateKeywordCampaignAlerts();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // 메모리 사용량 측정 종료
        System.gc(); // 가비지 컬렉션 유도
        Thread.sleep(1000);
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // 결과 검증
        long alertCount = alertRepository.count();
        long keywordCount = userKeywordRepository.findAllWithUser()
                .stream()
                .collect(Collectors.groupingBy(UserKeyword::getKeyword))
                .size();
        
        // 성능 분석 로깅
        logRealWorldPerformanceMetrics(totalTime, keywordCount, alertCount, memoryUsed);
        
        // 검증
        assertThat(alertCount).isGreaterThan(0);
        assertThat(totalTime).isLessThan(30000); // 30초 이내 완료
        
        log.info("=== 실제 스케줄러 시나리오 부하테스트 완료 ===");
    }

    private List<User> createTestUsers(int count) {
        List<User> users = IntStream.range(1, count + 1)
                .mapToObj(i -> {
                    UserPushSettings pushSettings = UserPushSettings.builder()
                            .pushEnabled(true)
                            .personalizedEnabled(true)
                            .marketingEnabled(true)
                            .serviceEnabled(true)
                            .activityEnabled(true)
                            .build();
                    
                    return User.builder()
                            .nickname("LoadTestUser" + i)
                            .email("loadtest" + i + "@test.com")
                            .isActive(true)
                            .pushSettings(pushSettings)
                            .build();
                })
                .collect(Collectors.toList());
        
        return userRepository.saveAll(users);
    }

    private List<String> generateTestKeywords(int count) {
        List<String> keywords = new ArrayList<>();
        
        // 다양한 패턴의 키워드 생성
        String[] categories = {"맛집", "여행", "쇼핑", "카페", "데이트", "운동", "문화", "교육", "게임", "영화"};
        String[] locations = {"강남", "홍대", "이태원", "명동", "압구정", "신촌", "건대", "잠실", "부산", "제주"};
        String[] adjectives = {"인기", "핫한", "트렌디", "유명", "숨은", "화제", "신규", "프리미엄", "특별", "한정"};
        
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            if (i < categories.length * locations.length) {
                // 카테고리 + 지역 조합
                String category = categories[i % categories.length];
                String location = locations[i / categories.length];
                keywords.add(location + " " + category);
            } else if (i < count - 100) {
                // 형용사 + 카테고리 조합
                String adjective = adjectives[random.nextInt(adjectives.length)];
                String category = categories[random.nextInt(categories.length)];
                keywords.add(adjective + " " + category);
            } else {
                // 단순 번호 키워드
                keywords.add("키워드" + i);
            }
        }
        
        return keywords;
    }

    private void createUserKeywordMappings() {
        Random random = new Random();
        List<UserKeyword> userKeywords = new ArrayList<>();
        
        for (User user : testUsers) {
            // 각 사용자마다 랜덤하게 키워드 선택
            Set<String> selectedKeywords = new HashSet<>();
            while (selectedKeywords.size() < KEYWORDS_PER_USER) {
                String keyword = testKeywords.get(random.nextInt(testKeywords.size()));
                selectedKeywords.add(keyword);
            }
            
            for (String keyword : selectedKeywords) {
                UserKeyword userKeyword = UserKeyword.builder()
                        .user(user)
                        .keyword(keyword)
                        .build();
                userKeywords.add(userKeyword);
            }
        }
        
        userKeywordRepository.saveAll(userKeywords);
    }


    private void logRealWorldPerformanceMetrics(long totalTime, long keywordCount, 
                                           long alertCount, long memoryUsed) {
        log.info("=== 실제 스케줄러 성능 분석 ===");
        log.info("1. 처리 시간: {}ms ({}초)", totalTime, String.format("%.2f", totalTime / 1000.0));
        log.info("2. 메모리 사용량: {}MB", String.format("%.2f", memoryUsed / (1024.0 * 1024.0)));
        log.info("3. 데이터베이스 처리:");
        log.info("   - 처리된 키워드 수: {}", keywordCount);
        log.info("   - 생성된 알림 수: {}", alertCount);
        log.info("4. 스레드풀 효율성:");
        log.info("   - 평균 키워드당 처리 시간: {}ms", String.format("%.2f", (double) totalTime / keywordCount));
        log.info("   - 초당 처리 키워드 수: {}", String.format("%.2f", keywordCount * 1000.0 / totalTime));
        log.info("   - 초당 처리 알림 수: {}", String.format("%.2f", alertCount * 1000.0 / totalTime));
        
        // 성능 기준 분석
        if (totalTime > 20000) {
            log.warn("처리 시간이 20초를 초과했습니다. 스레드풀 또는 DB 최적화 필요");
        } else if (totalTime > 10000) {
            log.info("처리 시간이 10-20초입니다. 적정 수준");
        } else {
            log.info("처리 시간이 10초 미만입니다. 매우 빠름");
        }
        
        if (memoryUsed > 100 * 1024 * 1024) { // 100MB
            log.warn("메모리 사용량이 100MB를 초과했습니다. 메모리 최적화 필요");
        }
        
        double alertRate = (double) alertCount / keywordCount * 100;
        log.info("5. 알림 생성률: {}% (키워드당 평균)", String.format("%.2f", alertRate));
    }
}