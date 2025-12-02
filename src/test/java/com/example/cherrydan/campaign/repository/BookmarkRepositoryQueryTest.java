package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class BookmarkRepositoryQueryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFCMTokenRepository userFCMTokenRepository;

    private User testUser;
    private Campaign testCampaign;
    private Bookmark testBookmark;
    private UserFCMToken testToken;

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .email("test-" + System.currentTimeMillis() + "@example.com")
                .name("테스트유저")
                .gender(Gender.MALE)
                .birthYear(1990)
                .isActive(true)
                .build();
        userRepository.save(testUser);

        testCampaign = Campaign.builder()
                .title("테스트 캠페인")
                .imageUrl("https://example.com/image.jpg")
                .detailUrl("https://example.com/detail-" + System.currentTimeMillis())
                .campaignType(CampaignType.REGION)
                .applyStart(LocalDate.now().minusDays(5))
                .applyEnd(LocalDate.now().plusDays(1))
                .isActive(true)
                .build();
        campaignRepository.save(testCampaign);

        testBookmark = Bookmark.builder()
                .user(testUser)
                .campaign(testCampaign)
                .isActive(true)
                .build();
        bookmarkRepository.save(testBookmark);

        testToken = UserFCMToken.builder()
                .userId(testUser.getId())
                .fcmToken("test-fcm-token-" + System.currentTimeMillis())
                .deviceType(DeviceType.ANDROID)
                .isAllowed(true)
                .isActive(true)
                .build();
        userFCMTokenRepository.save(testToken);
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        bookmarkRepository.deleteAll();
        campaignRepository.deleteAll();
        userFCMTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("findActiveBookmarksByApplyEndDate 쿼리가 정상적으로 실행되는지 테스트")
    void testFindActiveBookmarksByApplyEndDate() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Bookmark> result = bookmarkRepository.findActiveBookmarksByApplyEndDate(
                targetDate,
                pageRequest
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(testBookmark.getId());
        assertThat(result.getContent().get(0).getCampaign().getTitle()).isEqualTo("테스트 캠페인");
        assertThat(result.getContent().get(0).getUser().getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("알림이 비활성화된 사용자는 조회되지 않는지 테스트")
    void testFindActiveBookmarksByApplyEndDate_NotAllowedUser() {
        testToken.updateAllowedStatus(false);
        userFCMTokenRepository.save(testToken);

        LocalDate targetDate = LocalDate.now().plusDays(1);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Bookmark> result = bookmarkRepository.findActiveBookmarksByApplyEndDate(
                targetDate,
                pageRequest
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("비활성화된 캠페인은 조회되지 않는지 테스트")
    void testFindActiveBookmarksByApplyEndDate_InactiveCampaign() {
        Campaign inactiveCampaign = Campaign.builder()
                .title("비활성 캠페인")
                .imageUrl("https://example.com/image2.jpg")
                .detailUrl("https://example.com/detail2-" + System.currentTimeMillis())
                .campaignType(CampaignType.REGION)
                .applyStart(LocalDate.now().minusDays(5))
                .applyEnd(LocalDate.now().plusDays(1))
                .isActive(false)
                .build();
        campaignRepository.save(inactiveCampaign);

        Bookmark inactiveBookmark = Bookmark.builder()
                .user(testUser)
                .campaign(inactiveCampaign)
                .isActive(true)
                .build();
        bookmarkRepository.save(inactiveBookmark);

        LocalDate targetDate = LocalDate.now().plusDays(1);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Bookmark> result = bookmarkRepository.findActiveBookmarksByApplyEndDate(
                targetDate,
                pageRequest
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCampaign().getIsActive()).isTrue();
    }
}