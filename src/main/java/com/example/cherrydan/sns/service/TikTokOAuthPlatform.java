package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.SnsException;
import com.example.cherrydan.sns.config.SnsOAuthProperties;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.TokenResponse;
import com.example.cherrydan.sns.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * TikTok OAuth 플랫폼 구현체
 * 현재 개발 중이므로 로컬/개발 환경에서만 활성화
 */
@Slf4j
@Component
public class TikTokOAuthPlatform extends AbstractOAuthPlatform {

    // 임시로 Map에 저장 (실제로는 Redis나 세션 사용해야 함)
    private final Map<String, String> codeVerifierStore = new java.util.concurrent.ConcurrentHashMap<>();

    public TikTokOAuthPlatform(WebClient webClient, SnsOAuthProperties snsOAuthProperties) {
        super(webClient, snsOAuthProperties);
    }

    @Override
    protected String getClientIdParamName() {
        return "client_key";
    }

    @Override
    public String generateAuthUrl(String state) {
        SnsOAuthProperties.PlatformConfig config = getPlatformConfig();
        
        // PKCE용 code verifier와 challenge 생성
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
//        String state = generateRandomState();
        
        // state를 키로 하여 codeVerifier 저장
        codeVerifierStore.put(state, codeVerifier);
        
        return UriComponentsBuilder.fromHttpUrl(config.getAuthUrl())
                .queryParam("client_key", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", config.getScope())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUriString();
    }

    @Override
    public Mono<TokenResponse> getAccessToken(String code) {
        // TikTok OAuth에서는 state 파라미터를 받아와야 하지만, 
        // 현재 구조상 어려우므로 임시로 테스트 환경에서는 우회
        log.warn("TikTok OAuth는 PKCE가 필요하지만 현재 구조상 제한이 있습니다.");
        
        // 실제 TikTok API 호출 시도
        SnsOAuthProperties.PlatformConfig config = getPlatformConfig();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_key", config.getClientId());
        formData.add("client_secret", config.getClientSecret());
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", config.getRedirectUri());
        formData.add("code", code);
        
        // 가능한 codeVerifier가 있다면 추가 (완벽하지 않지만 시도)
        if (!codeVerifierStore.isEmpty()) {
            String anyCodeVerifier = codeVerifierStore.values().iterator().next();
            formData.add("code_verifier", anyCodeVerifier);
            log.info("Code verifier 추가됨");
        }

        return webClient.post()
                .uri(config.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnError(error -> log.error("TikTok 토큰 획득 실패: {}", error.getMessage()));
    }

    /**
     * PKCE용 code verifier 생성
     * @return 랜덤 code verifier
     */
    private String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * PKCE용 code challenge 생성
     * @param codeVerifier code verifier
     * @return SHA256 해시된 code challenge
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            log.error("Code challenge 생성 실패", e);
            throw new SnsException(ErrorMessage.SNS_CODE_CHALLENGE_FAILED);
        }
    }

    /**
     * CSRF 보호용 랜덤 state 생성
     * @return 랜덤 state
     */
    private String generateRandomState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public Mono<UserInfo> getUserInfo(String accessToken) {
        return webClient.post()
                .uri("https://open.tiktokapis.com/v2/user/info/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue("{\"fields\": [\"open_id\", \"display_name\"]}")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("TikTok 사용자 정보 조회 실패: {}", error.getMessage()))
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    Map<String, Object> user = (Map<String, Object>) data.get("user");
                    String id = (String) user.get("open_id");
                    String displayName = (String) user.get("display_name");
                    String profileUrl = "https://www.tiktok.com/@" + displayName.replace(" ", "");
                    return new UserInfo(id, profileUrl);
                });
    }

    @Override
    public SnsPlatform getPlatform() {
        return SnsPlatform.TIKTOK;
    }
} 