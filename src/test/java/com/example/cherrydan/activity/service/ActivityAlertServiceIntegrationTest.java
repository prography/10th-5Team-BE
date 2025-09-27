package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import com.example.cherrydan.fcm.dto.NotificationResultDto;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class ActivityAlertServiceIntegrationTest {

    @Autowired
    private ActivityAlertService activityAlertService;
    
    @Autowired
    private ActivityAlertRepository activityAlertRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private BookmarkRepository bookmarkRepository;
    
    @Autowired
    private CampaignStatusRepository campaignStatusRepository;
    
    @Autowired
    private UserFCMTokenRepository fcmTokenRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private User testUser;
    private final String TEST_FCM_TOKEN = "fNfetmFitk3Itehb2g2sxQ:APA91bEmxLFOpnVDHP-fha1rLWQNfAYPPL1qmIRIrSX4MddF3BUrAuDwoCpt4VGsC5EvBGHHBQdFTGWAZax-9v7z7UsFDhy1SDhP664zWctmdjw_dCiPm0I";
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .nickname("테스트유저")
                .email("test@example.com")
                .gender(Gender.MALE)
                .build();
        testUser = userRepository.save(testUser);
        
        // FCM 토큰 저장
        UserFCMToken fcmToken = UserFCMToken.builder()
                .userId(testUser.getId())
                .fcmToken(TEST_FCM_TOKEN)
                .deviceType(DeviceType.ANDROID)
                .isActive(true)
                .isAllowed(true)
                .build();
        fcmTokenRepository.save(fcmToken);
        
        System.out.println("=== 테스트 환경 설정 완료 ===");
        System.out.println("사용자 ID: " + testUser.getId());
        System.out.println("FCM 토큰: " + TEST_FCM_TOKEN);
    }
    
    @AfterEach
    void cleanup() {
        // 테스트 데이터 정리 (외래키 순서 고려)
        if (testUser == null) {
            return; // 사용자가 생성되지 않았으면 정리할 것 없음
        }
        
        try {
            // 알림 삭제
            activityAlertRepository.deleteAll();

            // 북마크와 상태 삭제
            bookmarkRepository.deleteAll();
            campaignStatusRepository.deleteAll();

            // 캠페인 삭제
            campaignRepository.deleteAll();

            // FCM 토큰 삭제
            fcmTokenRepository.deleteAll();
            
            // auth_token 테이블 직접 삭제 - EntityManagerFactory에서 EntityManager 가져와서 트랜잭션 처리
            EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
            try {
                em.getTransaction().begin();
                int deletedRows = em.createNativeQuery("DELETE FROM auth_token WHERE user_id = :userId")
                        .setParameter("userId", testUser.getId())
                        .executeUpdate();
                em.getTransaction().commit();
                System.out.println("auth_token 삭제 완료: " + deletedRows + "개 행");
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                System.err.println("auth_token 삭제 실패: " + e.getMessage());
            } finally {
                em.close();
            }

            // 사용자 삭제 시도 (실패해도 테스트 계속 진행)
            try {
                userRepository.deleteAll();
            } catch (Exception e) {
                System.err.println("사용자 삭제 실패 (auth_token 외래키 제약): " + e.getMessage());
                // 테스트는 계속 진행
            }
        } catch (Exception e) {
            System.err.println("테스트 데이터 정리 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    @DisplayName("BOOKMARK_DEADLINE_D1 - 북마크 D-1 알림 생성 및 FCM 전송 테스트")
    void testBookmarkDeadlineD1Alert() throws Exception {
        // Given: 내일 마감되는 캠페인 생성
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        Campaign campaign = createCampaign("테스트 캠페인", tomorrow, CampaignType.PRODUCT);
        Bookmark bookmark = createBookmark(testUser, campaign, true);

        // When: 알림 생성
        long startMemory = getUsedMemory();
        activityAlertService.updateActivityAlerts();
        
        // 비동기 처리 대기
        Thread.sleep(2000);
        
        long endMemory = getUsedMemory();
        System.out.println("메모리 사용량: " + (endMemory - startMemory) / 1024 / 1024 + " MB");
        
        // Then: 알림 확인
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.BOOKMARK_DEADLINE_D1);
        
        // FCM 전송 테스트 (실제 FCM 서버로 전송)
        System.out.println("알림 생성 성공: " + alerts.get(0).getAlertType());
        // sendFCMNotification은 자체 @Transactional이 있어서 안전함
        sendFCMNotification(alerts.get(0), "BOOKMARK_DEADLINE_D1");
    }
    
    @Test
    @DisplayName("BOOKMARK_DEADLINE_DDAY - 북마크 D-Day 알림 생성 및 FCM 전송 테스트")
    void testBookmarkDeadlineDDayAlert() throws Exception {
        // Given: 오늘 마감되는 캠페인 생성
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Campaign campaign = createCampaign("북마크 테스트 캠페인", today, CampaignType.PRODUCT);
        createBookmark(testUser, campaign, true);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.BOOKMARK_DEADLINE_DDAY);
        
        sendFCMNotification(alerts.get(0), "BOOKMARK_DEADLINE_DDAY");
    }
    
    @Test
    @DisplayName("APPLY_RESULT_DDAY - 선정 결과 D-Day 알림 생성 및 FCM 전송 테스트")
    void testApplyResultDDayAlert() throws Exception {
        // Given: 오늘 선정발표 캠페인 생성
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Campaign campaign = Campaign.builder()
                .title("선정발표 테스트 캠페인")
                .detailUrl("https://example.com/selection_test")
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(10))
                .applyEnd(today.minusDays(10))
                .reviewerAnnouncement(today)  // 오늘 선정발표
                .campaignType(CampaignType.PRODUCT)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        campaign = campaignRepository.save(campaign);
        
        // CampaignStatus 생성 (APPLIED)
        CampaignStatus status = CampaignStatus.builder()
                .user(testUser)
                .campaign(campaign)
                .status(CampaignStatusType.APPLY)
                .build();
        campaignStatusRepository.save(status);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.APPLY_RESULT_DDAY);
        
        sendFCMNotification(alerts.get(0), "APPLY_RESULT_DDAY");
    }
    
    @Test
    @DisplayName("SELECTED_VISIT_D3 - 방문 D-3 알림 생성 및 FCM 전송 테스트")
    void testSelectedVisitD3Alert() throws Exception {
        // Given: 3일 후 방문 마감 캠페인 생성 (REGION 타입)
        LocalDate visitDeadline = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3);
        Campaign campaign = Campaign.builder()
                .title("방문 테스트 캠페인 D-3")
                .detailUrl("https://example.com/visit_d3_test")
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(10))
                .applyEnd(visitDeadline.minusDays(10))
                .contentSubmissionEnd(visitDeadline)  // 3일 후 방문 마감
                .campaignType(CampaignType.REGION)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        campaign = campaignRepository.save(campaign);
        
        // CampaignStatus 생성 (SELECTED)
        CampaignStatus status = CampaignStatus.builder()
                .user(testUser)
                .campaign(campaign)
                .status(CampaignStatusType.SELECTED)
                .build();
        campaignStatusRepository.save(status);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.SELECTED_VISIT_D3);
        
        sendFCMNotification(alerts.get(0), "SELECTED_VISIT_D3");
    }
    
    @Test
    @DisplayName("SELECTED_VISIT_DDAY - 방문 D-Day 알림 생성 및 FCM 전송 테스트")
    void testSelectedVisitDDayAlert() throws Exception {
        // Given: 오늘 방문 마감 캠페인 생성 (REGION 타입)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Campaign campaign = Campaign.builder()
                .title("방문 D-Day 테스트 캠페인")
                .detailUrl("https://example.com/visit_dday_test")
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(10))
                .applyEnd(today.minusDays(10))
                .contentSubmissionEnd(today)  // 오늘 방문 마감
                .campaignType(CampaignType.REGION)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        campaign = campaignRepository.save(campaign);
        
        // CampaignStatus 생성 (SELECTED)
        CampaignStatus status = CampaignStatus.builder()
                .user(testUser)
                .campaign(campaign)
                .status(CampaignStatusType.SELECTED)
                .build();
        campaignStatusRepository.save(status);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.SELECTED_VISIT_DDAY);
        
        sendFCMNotification(alerts.get(0), "SELECTED_VISIT_DDAY");
    }
    
    @Test
    @DisplayName("REVIEWING_DEADLINE_D3 - 리뷰 작성 D-3 알림 생성 및 FCM 전송 테스트")
    void testReviewingDeadlineD3Alert() throws Exception {
        // Given: 3일 후 리뷰 마감 캠페인 생성
        LocalDate reviewDeadline = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3);
        Campaign campaign = Campaign.builder()
                .title("리뷰 D-3 테스트 캠페인")
                .detailUrl("https://example.com/review_d3_test")
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(20))
                .applyEnd(reviewDeadline.minusDays(20))
                .contentSubmissionEnd(reviewDeadline)  // 3일 후 리뷰 마감
                .campaignType(CampaignType.PRODUCT)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        campaign = campaignRepository.save(campaign);
        
        // CampaignStatus 생성 (REVIEWING)
        CampaignStatus status = CampaignStatus.builder()
                .user(testUser)
                .campaign(campaign)
                .status(CampaignStatusType.REVIEWING)
                .build();
        campaignStatusRepository.save(status);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.REVIEWING_DEADLINE_D3);
        
        sendFCMNotification(alerts.get(0), "REVIEWING_DEADLINE_D3");
    }
    
    @Test
    @DisplayName("REVIEWING_DEADLINE_DDAY - 리뷰 작성 D-Day 알림 생성 및 FCM 전송 테스트")
    void testReviewingDeadlineDDayAlert() throws Exception {
        // Given: 오늘 리뷰 마감 캠페인 생성
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Campaign campaign = Campaign.builder()
                .title("리뷰 D-Day 테스트 캠페인")
                .detailUrl("https://example.com/review_dday_test")
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(20))
                .applyEnd(today.minusDays(20))
                .contentSubmissionEnd(today)  // 오늘 리뷰 마감
                .campaignType(CampaignType.PRODUCT)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        campaign = campaignRepository.save(campaign);
        
        // CampaignStatus 생성 (REVIEWING)
        CampaignStatus status = CampaignStatus.builder()
                .user(testUser)
                .campaign(campaign)
                .status(CampaignStatusType.REVIEWING)
                .build();
        campaignStatusRepository.save(status);
        
        // When: 알림 생성
        activityAlertService.updateActivityAlerts();
        Thread.sleep(2000);
        
        // Then: 알림 확인 및 FCM 전송
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(ActivityAlertType.REVIEWING_DEADLINE_DDAY);
        
        sendFCMNotification(alerts.get(0), "REVIEWING_DEADLINE_DDAY");
    }
    
    
    @Test
    @DisplayName("모든 알림 타입 통합 테스트 - 대량 데이터 및 메모리 효율성 검증")
    void testAllAlertTypesWithLargeData() throws Exception {
        // Given: 대량 데이터 생성 (각 타입별 100개씩)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        
        System.out.println("=== 대량 데이터 생성 시작 ===");
        
        // 1. 북마크 D-1 알림용 데이터 (100개)
        for (int i = 0; i < 100; i++) {
            Campaign campaign = createCampaign("북마크 D-1 캠페인 " + i, 
                today.plusDays(1), CampaignType.PRODUCT);
            createBookmark(testUser, campaign, true);
        }
        
        // 2. 북마크 D-Day 알림용 데이터 (100개)
        for (int i = 0; i < 100; i++) {
            Campaign campaign = createCampaign("북마크 D-Day 캠페인 " + i, 
                today, CampaignType.PRODUCT);
            createBookmark(testUser, campaign, true);
        }
        
        // 3. 선정 결과 알림용 데이터 (100개)
        for (int i = 0; i < 100; i++) {
            Campaign campaign = Campaign.builder()
                    .title("선정결과 캠페인 " + i)
                    .detailUrl("https://example.com/selection_" + i)
                    .benefit("10000 포인트")
                    .imageUrl("https://example.com/image.jpg")
                    .recruitCount(100)
                    .applicantCount(0)
                    .applyStart(LocalDate.now().minusDays(10))
                    .applyEnd(today.minusDays(10))
                    .reviewerAnnouncement(today)  // 오늘 선정발표
                    .campaignType(CampaignType.PRODUCT)
                    .isActive(true)
                    .sourceSite("테스트")
                    .build();
            campaignRepository.save(campaign);
            
            CampaignStatus status = CampaignStatus.builder()
                    .user(testUser)
                    .campaign(campaign)
                    .status(CampaignStatusType.APPLY)
                    .build();
            campaignStatusRepository.save(status);
        }
        
        System.out.println("총 300개 테스트 데이터 생성 완료");
        
        // When: 알림 생성 (배치 처리)
        System.out.println("=== 배치 처리 시작 ===");
        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();
        
        activityAlertService.updateActivityAlerts();
        
        // 비동기 처리 완료 대기
        Thread.sleep(5000);
        
        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();
        
        // Then: 성능 측정 결과
        System.out.println("=== 성능 측정 결과 ===");
        System.out.println("처리 시간: " + (endTime - startTime) + " ms");
        System.out.println("메모리 사용량: " + (endMemory - startMemory) / 1024 / 1024 + " MB");
        
        // 알림 생성 확인
        List<ActivityAlert> alerts = activityAlertRepository.findByUserIdAndIsVisibleToUserTrue(testUser.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        System.out.println("생성된 알림 개수: " + alerts.size());
        
        // 타입별 카운트
        Map<ActivityAlertType, Long> typeCount = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ActivityAlert::getAlertType,
                    java.util.stream.Collectors.counting()
                ));
        
        System.out.println("=== 타입별 알림 개수 ===");
        typeCount.forEach((type, count) -> 
            System.out.println(type + ": " + count + "개"));
        
        // 샘플 FCM 전송 (첫 번째 알림만)
        if (!alerts.isEmpty()) {
            sendFCMNotification(alerts.get(0), "통합 테스트 샘플");
        }
    }
    
    // Helper 메서드들
    
    private Campaign createCampaign(String title, LocalDate applyEnd, CampaignType type) {
        Campaign campaign = Campaign.builder()
                .title(title)
                .detailUrl("https://example.com/" + title.replace(" ", "_") + "_" + System.currentTimeMillis())
                .benefit("10000 포인트")
                .imageUrl("https://example.com/image.jpg")
                .recruitCount(100)
                .applicantCount(0)
                .applyStart(LocalDate.now().minusDays(10))
                .applyEnd(applyEnd)
                .campaignType(type)
                .isActive(true)
                .sourceSite("테스트")
                .build();
        return campaignRepository.save(campaign);
    }
    
    private Bookmark createBookmark(User user, Campaign campaign, boolean isActive) {
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .campaign(campaign)
                .isActive(isActive)
                .build();
        return bookmarkRepository.save(bookmark);
    }
    
    protected void sendFCMNotification(ActivityAlert alert, String testType) {
        try {
            // JPQL로 fetch join을 사용해 Campaign까지 한번에 로드
            ActivityAlert freshAlert = entityManager.createQuery(
                    "SELECT a FROM ActivityAlert a " +
                    "JOIN FETCH a.campaign " +
                    "WHERE a.id = :id", ActivityAlert.class)
                    .setParameter("id", alert.getId())
                    .getSingleResult();
            
            Campaign campaign = freshAlert.getCampaign();
            String campaignTitle = campaign.getTitle();
            Long campaignId = campaign.getId();
            
            NotificationRequest request = NotificationRequest.builder()
                    .title(freshAlert.getAlertType().getTitle())
                    .body(freshAlert.getAlertType().getBodyTemplate(campaignTitle))
                    .data(Map.of(
                            "type", testType,
                            "alertId", String.valueOf(freshAlert.getId()),
                            "campaignId", String.valueOf(campaignId),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    ))
                    .priority("high")
                    .build();
            
            NotificationResultDto result = notificationService.sendNotificationToToken(
                TEST_FCM_TOKEN, request);
            
            System.out.println("=== FCM 전송 결과 (" + testType + ") ===");
            System.out.println("성공: " + (result.getSuccessCount() > 0));
            System.out.println("메시지: " + alert.getAlertType().getBodyTemplate(alert.getCampaign().getTitle()));
            
        } catch (Exception e) {
            System.out.println("FCM 전송 실패: " + e.getMessage());
        }
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}