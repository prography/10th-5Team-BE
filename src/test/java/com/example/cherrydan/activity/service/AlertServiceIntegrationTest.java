package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.activity.dto.UnreadAlertCountResponseDTO;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AlertServiceIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ActivityAlertRepository activityAlertRepository;

    @Autowired
    private KeywordCampaignAlertRepository keywordCampaignAlertRepository;

    private User testUser;
    private User otherUser;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .socialId("test123")
                .provider(AuthProvider.KAKAO)
                .role(Role.ROLE_USER)
                .gender(Gender.MALE)
                .isActive(true)
                .build());

        otherUser = userRepository.save(User.builder()
                .name("다른유저")
                .email("other@example.com")
                .socialId("other123")
                .provider(AuthProvider.KAKAO)
                .role(Role.ROLE_USER)
                .gender(Gender.FEMALE)
                .isActive(true)
                .build());

        testCampaign = campaignRepository.save(Campaign.builder()
                .title("테스트 캠페인")
                .detailUrl("https://test.com/campaign1")
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("미읽은 알림 개수를 정확히 조회한다")
    void getUnreadAlertCount_success() {
        // Given: 테스트 유저의 미읽은 알림 생성
        createActivityAlert(testUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_D1, false, true);  // 미읽음, 활동 알림
        createActivityAlert(testUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_DDAY, false, true);  // 미읽음, 활동 알림
        createKeywordAlert(testUser, "뷰티", false, true);  // 미읽음, 키워드 알림

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then
        assertThat(result.getTotalCount()).isEqualTo(3L);
        assertThat(result.getActivityAlertCount()).isEqualTo(2L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("읽은 알림은 개수에 포함되지 않는다")
    void getUnreadAlertCount_excludeReadAlerts() {
        // Given
        createActivityAlert(testUser, testCampaign, ActivityAlertType.APPLY_RESULT_DDAY, false, true);  // 미읽음
        createActivityAlert(testUser, testCampaign, ActivityAlertType.SELECTED_VISIT_D3, true, true);   // 읽음
        createKeywordAlert(testUser, "맛집", false, true);  // 미읽음
        createKeywordAlert(testUser, "카페", true, true);   // 읽음

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then
        assertThat(result.getTotalCount()).isEqualTo(2L);
        assertThat(result.getActivityAlertCount()).isEqualTo(1L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("보이지 않는 알림은 개수에 포함되지 않는다")
    void getUnreadAlertCount_excludeInvisibleAlerts() {
        // Given
        createActivityAlert(testUser, testCampaign, ActivityAlertType.SELECTED_VISIT_DDAY, false, true);   // 보임
        createActivityAlert(testUser, testCampaign, ActivityAlertType.REVIEWING_DEADLINE_D3, false, false);  // 숨김
        createKeywordAlert(testUser, "뷰티", false, true);   // 보임
        createKeywordAlert(testUser, "맛집", false, false);  // 숨김

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then
        assertThat(result.getTotalCount()).isEqualTo(2L);
        assertThat(result.getActivityAlertCount()).isEqualTo(1L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 사용자의 알림은 개수에 포함되지 않는다")
    void getUnreadAlertCount_excludeOtherUserAlerts() {
        // Given: 테스트 유저의 알림
        createActivityAlert(testUser, testCampaign, ActivityAlertType.REVIEWING_DEADLINE_DDAY, false, true);
        createKeywordAlert(testUser, "뷰티", false, true);

        // 다른 유저의 알림
        createActivityAlert(otherUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_D1, false, true);
        createKeywordAlert(otherUser, "맛집", false, true);

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then: 테스트 유저의 알림만 카운트
        assertThat(result.getTotalCount()).isEqualTo(2L);
        assertThat(result.getActivityAlertCount()).isEqualTo(1L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("미읽은 알림이 없으면 0을 반환한다")
    void getUnreadAlertCount_noUnreadAlerts() {
        // Given: 읽은 알림만 존재
        createActivityAlert(testUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_DDAY, true, true);
        createKeywordAlert(testUser, "뷰티", true, true);

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then
        assertThat(result.getTotalCount()).isEqualTo(0L);
        assertThat(result.getActivityAlertCount()).isEqualTo(0L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("알림이 전혀 없으면 0을 반환한다")
    void getUnreadAlertCount_noAlerts() {
        // Given: 알림 없음

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then
        assertThat(result.getTotalCount()).isEqualTo(0L);
        assertThat(result.getActivityAlertCount()).isEqualTo(0L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("복합 조건 테스트: 미읽음, 읽음, 숨김, 다른 유저 알림이 섞여있을 때")
    void getUnreadAlertCount_complexScenario() {
        // Given
        // 테스트 유저의 미읽은 알림 (카운트 O)
        createActivityAlert(testUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_D1, false, true);
        createActivityAlert(testUser, testCampaign, ActivityAlertType.BOOKMARK_DEADLINE_DDAY, false, true);
        createKeywordAlert(testUser, "뷰티", false, true);

        // 테스트 유저의 읽은 알림 (카운트 X)
        createActivityAlert(testUser, testCampaign, ActivityAlertType.APPLY_RESULT_DDAY, true, true);
        createKeywordAlert(testUser, "맛집", true, true);

        // 테스트 유저의 숨김 알림 (카운트 X)
        createActivityAlert(testUser, testCampaign, ActivityAlertType.SELECTED_VISIT_D3, false, false);
        createKeywordAlert(testUser, "카페", false, false);

        // 다른 유저의 미읽은 알림 (카운트 X)
        createActivityAlert(otherUser, testCampaign, ActivityAlertType.SELECTED_VISIT_DDAY, false, true);
        createKeywordAlert(otherUser, "서울", false, true);

        // When
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(testUser.getId());

        // Then: 테스트 유저의 미읽은 + 보이는 알림만 카운트
        assertThat(result.getTotalCount()).isEqualTo(3L);
        assertThat(result.getActivityAlertCount()).isEqualTo(2L);
        assertThat(result.getKeywordAlertCount()).isEqualTo(1L);
    }

    private void createActivityAlert(User user, Campaign campaign, ActivityAlertType alertType, boolean isRead, boolean isVisible) {
        ActivityAlert alert = ActivityAlert.builder()
                .user(user)
                .campaign(campaign)
                .alertDate(LocalDate.now())
                .alertType(alertType)
                .alertStage(0)
                .isVisibleToUser(isVisible)
                .isRead(isRead)
                .build();
        activityAlertRepository.save(alert);
    }

    private void createKeywordAlert(User user, String keyword, boolean isRead, boolean isVisible) {
        KeywordCampaignAlert alert = KeywordCampaignAlert.builder()
                .user(user)
                .keyword(keyword)
                .campaignCount(10)
                .alertDate(LocalDate.now())
                .alertStage(0)
                .isVisibleToUser(isVisible)
                .isRead(isRead)
                .build();
        keywordCampaignAlertRepository.save(alert);
    }
}
