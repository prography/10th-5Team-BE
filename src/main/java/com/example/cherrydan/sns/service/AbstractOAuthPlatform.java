package com.example.cherrydan.sns.service;

import com.example.cherrydan.sns.config.SnsOAuthProperties;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.TokenResponse;
import com.example.cherrydan.sns.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * OAuth 플랫폼 구현을 위한 추상 클래스
 * 공통적인 OAuth 로직을 제공하고, 플랫폼별 특화 로직은 하위 클래스에서 구현합니다.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOAuthPlatform implements OAuthPlatform {

    protected final WebClient webClient;
    protected final SnsOAuthProperties snsOAuthProperties;

    @Override
    public String generateAuthUrl() {
        SnsOAuthProperties.PlatformConfig config = getPlatformConfig();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(config.getAuthUrl())
                .queryParam(getClientIdParamName(), config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", config.getScope())
                .queryParam("response_type", "code");

        // 플랫폼별 추가 파라미터 설정
        addAuthUrlParameters(builder, config);

        return builder.build().toUriString();
    }

    @Override
    public Mono<TokenResponse> getAccessToken(String code) {
        SnsOAuthProperties.PlatformConfig config = getPlatformConfig();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(getClientIdParamName(), config.getClientId());
        formData.add("client_secret", config.getClientSecret());
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", config.getRedirectUri());
        formData.add("code", code);

        // 플랫폼별 추가 파라미터 설정
        addTokenRequestParameters(formData, config);

        return webClient.post()
                .uri(config.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnError(error -> log.error("{} 토큰 획득 실패: {}", getPlatform(), error.getMessage()));
    }

    @Override
    public Mono<UserInfo> getUserInfo(String accessToken) {
        // 기본 구현은 하위 클래스에서 오버라이드
        return Mono.error(new UnsupportedOperationException("getUserInfo must be implemented by subclass"));
    }

    /**
     * 플랫폼별 설정을 가져옵니다.
     * @return 플랫폼 설정
     */
    protected SnsOAuthProperties.PlatformConfig getPlatformConfig() {
        SnsOAuthProperties.PlatformConfig config = snsOAuthProperties.getPlatformConfig(getPlatformCode());
        if (config == null) {
            throw new RuntimeException(getPlatform() + " 설정을 찾을 수 없습니다.");
        }
        return config;
    }

    /**
     * 클라이언트 ID 파라미터 이름을 반환합니다.
     * 기본값은 "client_id"이며, 필요시 하위 클래스에서 오버라이드할 수 있습니다.
     * @return 클라이언트 ID 파라미터 이름
     */
    protected String getClientIdParamName() {
        return "client_id";
    }

    /**
     * 인증 URL 생성 시 플랫폼별 추가 파라미터를 설정합니다.
     * @param builder UriComponentsBuilder
     * @param config 플랫폼 설정
     */
    protected void addAuthUrlParameters(UriComponentsBuilder builder, SnsOAuthProperties.PlatformConfig config) {
        // 기본 구현은 빈 메소드, 필요시 하위 클래스에서 오버라이드
    }

    /**
     * 토큰 요청 시 플랫폼별 추가 파라미터를 설정합니다.
     * @param formData 폼 데이터
     * @param config 플랫폼 설정
     */
    protected void addTokenRequestParameters(MultiValueMap<String, String> formData, SnsOAuthProperties.PlatformConfig config) {
        // 기본 구현은 빈 메소드, 필요시 하위 클래스에서 오버라이드
    }
} 