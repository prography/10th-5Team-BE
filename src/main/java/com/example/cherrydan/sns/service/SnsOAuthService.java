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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                                    user, platform, userInfo.getId(), userInfo.getUrl(),
                                    tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(),
                                    calculateExpiresAt(tokenResponse.getExpiresIn())
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
     * OAuth 인증 URL을 생성합니다.
     * @param platform SNS 플랫폼
     * @return 인증 URL
     */
    public String getAuthUrl(SnsPlatform platform) {
        OAuthPlatform oauthPlatform = getOAuthPlatform(platform);
        return oauthPlatform.generateAuthUrl();
    }

    /**
     * 사용자의 SNS 연동 목록을 조회합니다.
     * @param user 사용자
     * @return 연동 목록
     */
    @Transactional(readOnly = true)
    public List<SnsConnectionResponse> getUserSnsConnections(User user) {
        // 활성 사용자인지 확인
        if (!user.getIsActive()) {
            throw new SnsException(ErrorMessage.USER_NOT_FOUND);
        }
        
        Map<SnsPlatform, SnsConnection> connectionMap = snsConnectionRepository.findAllByUser(user)
                .stream()
                .filter(SnsConnection::getIsActive)
                .collect(Collectors.toMap(SnsConnection::getPlatform, c -> c));

        return getAvailablePlatforms()
                .stream()
                .map(platform -> {
                    SnsConnection connection = connectionMap.get(platform);
                    return connection != null ?
                            SnsConnectionResponse.from(connection) :
                            SnsConnectionResponse.notConnected(platform);
                })
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
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresAt 만료 시간
     * @return SNS 연동 정보
     */
    private SnsConnection createOrUpdateConnection(User user, SnsPlatform platform, String snsUserId, 
                                                 String snsUrl, String accessToken, String refreshToken, 
                                                 LocalDateTime expiresAt) {
        SnsConnection connection = snsConnectionRepository.findByUserAndPlatformIgnoreActive(user, platform)
                .orElseGet(() -> SnsConnection.builder().user(user).platform(platform).build());
        
        connection.updateSnsInfo(snsUserId, snsUrl, accessToken, refreshToken, expiresAt);
        connection.setIsActive(true);
        
        return snsConnectionRepository.save(connection);
    }

    /**
     * 토큰 만료 시간을 계산합니다.
     * @param expiresIn 만료 시간(초)
     * @return 만료 시간
     */
    private LocalDateTime calculateExpiresAt(Integer expiresIn) {
        return expiresIn != null ? 
                LocalDateTime.now().plusSeconds(expiresIn) : 
                LocalDateTime.now().plusYears(1); // 기본값 1년
    }
} 