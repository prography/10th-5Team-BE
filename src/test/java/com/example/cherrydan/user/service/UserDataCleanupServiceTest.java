package com.example.cherrydan.user.service;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.inquiry.domain.Inquiry;
import com.example.cherrydan.inquiry.domain.InquiryCategory;
import com.example.cherrydan.inquiry.repository.InquiryRepository;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.domain.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class UserDataCleanupServiceTest {

    @Autowired
    private UserDataCleanupService userDataCleanupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SnsConnectionRepository snsConnectionRepository;

    @Autowired
    private CampaignStatusRepository campaignStatusRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ActivityAlertRepository activityAlertRepository;

    @Autowired
    private KeywordCampaignAlertRepository keywordCampaignAlertRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserFCMTokenRepository userFCMTokenRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private User expiredUser;
    private User recentUser;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        testCampaign = Campaign.builder()
                .title("테스트 캠페인")
                .detailUrl("https://test.com/campaign/" + System.currentTimeMillis())
                .applyEnd(LocalDate.now().plusDays(7))
                .isActive(true)
                .build();
        testCampaign = campaignRepository.save(testCampaign);

        expiredUser = User.builder()
                .email("expired@test.com")
                .name("만료된유저")
                .provider(AuthProvider.KAKAO)
                .socialId("expired123")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
        expiredUser.softDelete();
        expiredUser.setDeletedAt(LocalDateTime.now().minusDays(366));
        expiredUser = userRepository.save(expiredUser);

        recentUser = User.builder()
                .email("recent@test.com")
                .name("최근삭제유저")
                .provider(AuthProvider.KAKAO)
                .socialId("recent123")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
        recentUser.softDelete();
        recentUser.setDeletedAt(LocalDateTime.now().minusDays(180));
        recentUser = userRepository.save(recentUser);

        createUserRelatedData(expiredUser);
        createUserRelatedData(recentUser);
    }

    private void createUserRelatedData(User user) {
        SnsConnection snsConnection = SnsConnection.builder()
                .user(user)
                .platform(SnsPlatform.INSTAGRAM)
                .snsUserId("test_sns_id_" + user.getId())
                .isActive(true)
                .build();
        snsConnectionRepository.save(snsConnection);

        CampaignStatus campaignStatus = CampaignStatus.builder()
                .user(user)
                .campaign(testCampaign)
                .status(CampaignStatusType.APPLY)
                .isActive(true)
                .build();
        campaignStatusRepository.save(campaignStatus);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .category(InquiryCategory.OTHER)
                .title("문의 제목")
                .content("문의 내용")
                .build();
        inquiryRepository.save(inquiry);

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .campaign(testCampaign)
                .isActive(true)
                .build();
        bookmarkRepository.save(bookmark);

        ActivityAlert activityAlert = ActivityAlert.builder()
                .user(user)
                .campaign(testCampaign)
                .alertDate(LocalDate.now())
                .alertType(ActivityAlertType.APPLY_RESULT_DDAY)
                .build();
        activityAlertRepository.save(activityAlert);

        KeywordCampaignAlert keywordAlert = KeywordCampaignAlert.builder()
                .user(user)
                .keyword("테스트")
                .campaignCount(10)
                .alertDate(LocalDate.now())
                .build();
        keywordCampaignAlertRepository.save(keywordAlert);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken("test_refresh_token_" + user.getId())
                .build();
        refreshTokenRepository.save(refreshToken);

        UserFCMToken fcmToken = UserFCMToken.builder()
                .userId(user.getId())
                .fcmToken("test_fcm_token_" + user.getId())
                .deviceModel("iPhone 14")
                .deviceType(DeviceType.IOS)
                .isActive(true)
                .isAllowed(true)
                .build();
        userFCMTokenRepository.save(fcmToken);
    }

    @Test
    @DisplayName("1년 경과한 유저의 연관 데이터가 모두 삭제된다")
    void cleanupExpiredUserData_deletesAllRelatedData() {
        Long expiredUserId = expiredUser.getId();

        assertThat(snsConnectionRepository.findByUserId(expiredUserId)).isNotEmpty();
        assertThat(campaignStatusRepository.findByUserAndIsActiveTrue(expiredUser)).isNotEmpty();
        assertThat(refreshTokenRepository.findByUserId(expiredUserId)).isPresent();
        assertThat(userFCMTokenRepository.findByUserId(expiredUserId)).isNotEmpty();

        userDataCleanupService.cleanupExpiredUserData();

        assertThat(snsConnectionRepository.findByUserId(expiredUserId)).isEmpty();
        assertThat(campaignStatusRepository.findByUserAndIsActiveTrue(expiredUser)).isEmpty();
        assertThat(refreshTokenRepository.findByUserId(expiredUserId)).isEmpty();
        assertThat(userFCMTokenRepository.findByUserId(expiredUserId)).isEmpty();
        assertThat(activityAlertRepository.countByUserIdAndIsVisibleToUserTrue(expiredUserId)).isZero();
        assertThat(keywordCampaignAlertRepository.countUnreadByUserId(expiredUserId)).isZero();
    }

    @Test
    @DisplayName("1년 이내에 삭제된 유저의 데이터는 삭제되지 않는다")
    void cleanupExpiredUserData_doesNotDeleteRecentUsers() {
        Long recentUserId = recentUser.getId();

        long beforeSnsCount = snsConnectionRepository.findByUserId(recentUserId).size();
        long beforeBookmarkCount = bookmarkRepository.findAllByUserIdAndIsActiveTrue(recentUserId).size();

        userDataCleanupService.cleanupExpiredUserData();

        long afterSnsCount = snsConnectionRepository.findByUserId(recentUserId).size();
        long afterBookmarkCount = bookmarkRepository.findAllByUserIdAndIsActiveTrue(recentUserId).size();

        assertThat(afterSnsCount).isEqualTo(beforeSnsCount);
        assertThat(afterBookmarkCount).isEqualTo(beforeBookmarkCount);
    }

    @Test
    @DisplayName("User 엔티티는 소프트 딜리트 상태로 유지된다")
    void cleanupExpiredUserData_keepsUserInSoftDeletedState() {
        Long expiredUserId = expiredUser.getId();

        userDataCleanupService.cleanupExpiredUserData();

        User user = userRepository.findById(expiredUserId).orElseThrow();
        assertThat(user.getIsActive()).isFalse();
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("1년 이내 유저는 복구 가능하다")
    void isRestorableWithin1Year_returnsTrueForRecentUsers() {
        assertThat(recentUser.isRestorableWithin1Year()).isTrue();
    }

    @Test
    @DisplayName("1년 경과 유저는 복구 불가능하다")
    void isRestorableWithin1Year_returnsFalseForExpiredUsers() {
        assertThat(expiredUser.isRestorableWithin1Year()).isFalse();
    }

    @Test
    @DisplayName("복구 기능이 정상 동작한다")
    void restore_restoresUserSuccessfully() {
        recentUser.restore();
        userRepository.save(recentUser);

        User restored = userRepository.findActiveById(recentUser.getId()).orElseThrow();
        assertThat(restored.getIsActive()).isTrue();
        assertThat(restored.getDeletedAt()).isNull();
    }
}
