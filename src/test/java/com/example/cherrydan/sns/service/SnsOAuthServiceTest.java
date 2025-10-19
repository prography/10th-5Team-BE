package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.SnsException;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.SnsConnectionResponse;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class SnsOAuthServiceTest {

    @Autowired
    private SnsOAuthService snsOAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SnsConnectionRepository snsConnectionRepository;

    @Autowired
    private OAuthStateService oAuthStateService;

    private User activeUser;
    private User inactiveUser;
    private SnsConnection youtubeConnection;
    private SnsConnection instagramConnection;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .email("active@test.com")
                .name("ActiveUser")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
        userRepository.save(activeUser);

        inactiveUser = User.builder()
                .email("inactive@test.com")
                .name("InactiveUser")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
        inactiveUser.setDeletedAt(LocalDateTime.now());
        userRepository.save(inactiveUser);

        youtubeConnection = SnsConnection.builder()
                .user(activeUser)
                .platform(SnsPlatform.YOUTUBE)
                .snsUserId("youtube123")
                .snsUrl("https://youtube.com/@test")
                .isActive(true)
                .build();
        snsConnectionRepository.save(youtubeConnection);

        instagramConnection = SnsConnection.builder()
                .user(activeUser)
                .platform(SnsPlatform.INSTAGRAM)
                .snsUserId("instagram123")
                .snsUrl("https://instagram.com/test")
                .isActive(true)
                .build();
        snsConnectionRepository.save(instagramConnection);
    }

    @Test
    @DisplayName("연동된 SNS 목록을 조회합니다")
    void getUserSnsConnections_성공_연동된_SNS_목록_반환() {
        List<SnsConnectionResponse> connections = snsOAuthService.getUserSnsConnections(activeUser);

        assertThat(connections).hasSize(2);
        assertThat(connections)
                .extracting(SnsConnectionResponse::getPlatform)
                .containsExactlyInAnyOrder(SnsPlatform.YOUTUBE, SnsPlatform.INSTAGRAM);
    }

    @Test
    @DisplayName("비활성 사용자의 SNS 목록 조회 시 예외가 발생합니다")
    void getUserSnsConnections_비활성_사용자_예외_발생() {
        assertThatThrownBy(() -> snsOAuthService.getUserSnsConnections(inactiveUser))
                .isInstanceOf(SnsException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("SNS 연동을 해제합니다")
    void disconnectSns_성공_연동_해제() {
        snsOAuthService.disconnectSns(activeUser, SnsPlatform.YOUTUBE);

        SnsConnection connection = snsConnectionRepository.findByUserAndPlatformIgnoreActive(activeUser, SnsPlatform.YOUTUBE)
                .orElseThrow();

        assertThat(connection.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("연동되지 않은 플랫폼 해제 시 예외가 발생합니다")
    void disconnectSns_연동되지_않은_플랫폼_예외_발생() {
        assertThatThrownBy(() -> snsOAuthService.disconnectSns(activeUser, SnsPlatform.TIKTOK))
                .isInstanceOf(SnsException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.SNS_CONNECTION_NOT_FOUND);
    }

    @Test
    @DisplayName("비활성 사용자의 SNS 연동 해제 시 예외가 발생합니다")
    void disconnectSns_비활성_사용자_예외_발생() {
        assertThatThrownBy(() -> snsOAuthService.disconnectSns(inactiveUser, SnsPlatform.YOUTUBE))
                .isInstanceOf(SnsException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("SNS 연동 해제 후 목록 조회 시 제외됩니다")
    void disconnectSns_해제_후_목록에서_제외() {
        snsOAuthService.disconnectSns(activeUser, SnsPlatform.YOUTUBE);

        List<SnsConnectionResponse> connections = snsOAuthService.getUserSnsConnections(activeUser);

        assertThat(connections).hasSize(1);
        assertThat(connections.get(0).getPlatform()).isEqualTo(SnsPlatform.INSTAGRAM);
    }

    @Test
    @DisplayName("OAuth 인증 URL에 state가 포함되어 생성됩니다")
    void getAuthUrlWithState_성공_state_포함_URL_생성() {
        String authUrl = snsOAuthService.getAuthUrlWithState(SnsPlatform.YOUTUBE, activeUser.getId());

        assertThat(authUrl).isNotNull();
        assertThat(authUrl).contains("state=");
        assertThat(authUrl).contains("client_id=");
        assertThat(authUrl).contains("redirect_uri=");
    }

    @Test
    @DisplayName("생성된 state를 파싱하면 원래 userId를 얻을 수 있습니다")
    void oAuthState_생성_및_파싱_정상_동작() {
        String state = oAuthStateService.createState(activeUser.getId());
        Long parsedUserId = oAuthStateService.parseState(state);

        assertThat(parsedUserId).isEqualTo(activeUser.getId());
    }

    @Test
    @DisplayName("해제된 SNS를 다시 연동할 수 있습니다")
    void disconnectSns_해제_후_재연동_가능() {
        snsOAuthService.disconnectSns(activeUser, SnsPlatform.YOUTUBE);

        SnsConnection deactivated = snsConnectionRepository.findByUserAndPlatformIgnoreActive(activeUser, SnsPlatform.YOUTUBE)
                .orElseThrow();
        assertThat(deactivated.getIsActive()).isFalse();

        deactivated.updateSnsInfo("new_youtube_id", "https://youtube.com/@newtest");
        deactivated.setIsActive(true);
        snsConnectionRepository.save(deactivated);

        SnsConnection reactivated = snsConnectionRepository.findByUserAndPlatform(activeUser, SnsPlatform.YOUTUBE)
                .orElseThrow();
        assertThat(reactivated.getIsActive()).isTrue();
        assertThat(reactivated.getSnsUserId()).isEqualTo("new_youtube_id");
    }

    @Test
    @DisplayName("Repository의 findByUser는 활성 연동만 반환합니다")
    void repository_findByUser_활성_연동만_반환() {
        youtubeConnection.deactivate();
        snsConnectionRepository.save(youtubeConnection);

        List<SnsConnection> connections = snsConnectionRepository.findByUser(activeUser);

        assertThat(connections).hasSize(1);
        assertThat(connections.get(0).getPlatform()).isEqualTo(SnsPlatform.INSTAGRAM);
    }

    @Test
    @DisplayName("Repository의 findByUserAndPlatformIgnoreActive는 비활성 포함 조회합니다")
    void repository_findByUserAndPlatformIgnoreActive_비활성_포함_조회() {
        youtubeConnection.deactivate();
        snsConnectionRepository.save(youtubeConnection);

        SnsConnection connection = snsConnectionRepository.findByUserAndPlatformIgnoreActive(activeUser, SnsPlatform.YOUTUBE)
                .orElseThrow();

        assertThat(connection.getIsActive()).isFalse();
    }
}
