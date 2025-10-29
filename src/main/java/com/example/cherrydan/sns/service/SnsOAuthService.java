package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.SnsException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.SnsConnectionResponse;
import com.example.cherrydan.sns.dto.TokenResponse;
import com.example.cherrydan.sns.dto.UserInfo;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SNS OAuth 연동 서비스
 * 다양한 SNS 플랫폼과의 OAuth 인증 및 연동을 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SnsOAuthService {

    private final SnsConnectionRepository snsConnectionRepository;
    private final Map<SnsPlatform, OAuthPlatform> oauthPlatforms;
    private final OAuthStateService oAuthStateService;
    private final UserService userService;

    /**
     * OAuth 콜백을 처리합니다.
     * State 파싱, 사용자 조회, SNS 연동을 한 번에 처리합니다.
     * @param code OAuth 인증 코드
     * @param state OAuth state (userId 암호화)
     * @param platform SNS 플랫폼
     * @return 연동 결과
     */
    public Mono<SnsConnectionResponse> handleCallback(String code, String state, SnsPlatform platform) {
        Long userId = oAuthStateService.parseState(state);
        User user = userService.getUserById(userId);
        return connect(user, code, platform);
    }

    /**
     * OAuth 인증을 통해 SNS 플랫폼과 연동합니다.
     * @param user 사용자
     * @param code OAuth 인증 코드
     * @param platform SNS 플랫폼
     * @return 연동 결과
     */
    public Mono<SnsConnectionResponse> connect(User user, String code, SnsPlatform platform) {
        // 활성 사용자인지 확인
        if (!user.getIsActive()) {
            return Mono.error(new SnsException(ErrorMessage.USER_NOT_FOUND));
        }
        
        OAuthPlatform oauthPlatform = getOAuthPlatform(platform);

        return oauthPlatform.getAccessToken(code)
                .flatMap(tokenResponse -> oauthPlatform.getUserInfo(tokenResponse.getAccessToken())
                        .map(userInfo -> {
                            SnsConnection connection = createOrUpdateConnection(
                                    user, platform, userInfo.getId(), userInfo.getUrl()
                            );

                            log.info("{} 연동 완료: user={}, userId={}", platform, user.getId(), userInfo.getId());
                            return SnsConnectionResponse.from(connection);
                        }))
                .onErrorMap(error -> {
                    log.error("{} 연동 실패: user={}, error={}", platform, user.getId(), error.getMessage());
                    return new SnsException(ErrorMessage.SNS_CONNECTION_FAILED, error.getMessage());
                });
    }

    /**
     * OAuth 인증 URL을 생성합니다 (userId로 state 자동 생성).
     * @param platform SNS 플랫폼
     * @param userId 사용자 ID
     * @return 인증 URL
     */
    public String getAuthUrlWithState(SnsPlatform platform, Long userId) {
        String state = oAuthStateService.createState(userId);
        return getAuthUrl(platform, state);
    }

    /**
     * OAuth 인증 URL을 생성합니다.
     * @param platform SNS 플랫폼
     * @param state OAuth state 파라미터 (CSRF 방지 및 사용자 식별용)
     * @return 인증 URL
     */
    public String getAuthUrl(SnsPlatform platform, String state) {
        OAuthPlatform oauthPlatform = getOAuthPlatform(platform);
        return oauthPlatform.generateAuthUrl(state);
    }

    /**
     * 사용자의 SNS 연동 목록을 조회합니다.
     * @param user 사용자
     * @return 연동 목록
     */
    public List<SnsConnectionResponse> getUserSnsConnections(User user) {
        if (!user.getIsActive()) {
            throw new SnsException(ErrorMessage.USER_NOT_FOUND);
        }
        return snsConnectionRepository.findByUser(user)
                .stream()
                .map(SnsConnectionResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 현재 환경에서 사용 가능한 플랫폼 목록을 반환합니다.
     * @return 사용 가능한 플랫폼 목록
     */
    private List<SnsPlatform> getAvailablePlatforms() {
        // 프로덕션 환경에서는 YouTube만 표시
        String profile = System.getProperty("spring.profiles.active", "local");
        
        if ("prod".equals(profile) || "production".equals(profile)) {
            log.info("프로덕션 환경: YouTube만 지원");
            return List.of(SnsPlatform.YOUTUBE);
        } else {
            log.info("개발 환경: 모든 플랫폼 지원");
            return Arrays.asList(SnsPlatform.values());
        }
    }

    /**
     * SNS 연동을 해제합니다.
     * @param user 사용자
     * @param platform SNS 플랫폼
     */
    public void disconnectSns(User user, SnsPlatform platform) {
        // 활성 사용자인지 확인
        if (!user.getIsActive()) {
            throw new SnsException(ErrorMessage.USER_NOT_FOUND);
        }
        
        SnsConnection connection = snsConnectionRepository
                .findByUserAndPlatform(user, platform)
                .orElseThrow(() -> new SnsException(ErrorMessage.SNS_CONNECTION_NOT_FOUND));

        connection.deactivate();
        snsConnectionRepository.save(connection);

        log.info("SNS 연동 해제 완료: user={}, platform={}", user.getId(), platform);
    }

    /**
     * OAuth 플랫폼을 가져옵니다.
     * @param platform SNS 플랫폼
     * @return OAuth 플랫폼
     */
    private OAuthPlatform getOAuthPlatform(SnsPlatform platform) {
        OAuthPlatform oauthPlatform = oauthPlatforms.get(platform);
        if (oauthPlatform == null) {
            throw new SnsException(ErrorMessage.SNS_PLATFORM_NOT_SUPPORTED, "Platform: " + platform);
        }
        return oauthPlatform;
    }

    /**
     * SNS 연동 정보를 생성하거나 업데이트합니다.
     * @param user 사용자
     * @param platform SNS 플랫폼
     * @param snsUserId SNS 사용자 ID
     * @param snsUrl SNS URL
     * @return SNS 연동 정보
     */
    private SnsConnection createOrUpdateConnection(User user, SnsPlatform platform, String snsUserId, String snsUrl) {
        SnsConnection connection = snsConnectionRepository.findByUserAndPlatformIgnoreActive(user, platform)
                .orElseGet(() -> SnsConnection.builder().user(user).platform(platform).build());

        connection.updateSnsInfo(snsUserId, snsUrl);
        connection.setIsActive(true);

        return snsConnectionRepository.save(connection);
    }
} 