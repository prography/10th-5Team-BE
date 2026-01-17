package com.example.cherrydan.sns.service;

import com.example.cherrydan.sns.config.SnsOAuthProperties;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Instagram OAuth 플랫폼 구현체
 * 현재 개발 중이므로 로컬/개발 환경에서만 활성화
 */
@Slf4j
@Component
public class InstagramOAuthPlatform extends AbstractOAuthPlatform {

    public InstagramOAuthPlatform(WebClient webClient, SnsOAuthProperties snsOAuthProperties) {
        super(webClient, snsOAuthProperties);
    }

    @Override
    public Mono<UserInfo> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://graph.instagram.com/me?fields=id,username&access_token=" + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("Instagram 사용자 정보 조회 실패: {}", error.getMessage()))
                .map(response -> {
                    String id = (String) response.get("id");
                    String username = (String) response.get("username");
                    String profileUrl = "https://www.instagram.com/" + username;
                    return new UserInfo(id, profileUrl);
                });
    }

    @Override
    public SnsPlatform getPlatform() {
        return SnsPlatform.INSTAGRAM;
    }
} 