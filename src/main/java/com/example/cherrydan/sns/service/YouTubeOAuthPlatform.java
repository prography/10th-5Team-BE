package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.SnsException;
import com.example.cherrydan.sns.config.SnsOAuthProperties;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * YouTube OAuth 플랫폼 구현체
 */
@Slf4j
@Component
public class YouTubeOAuthPlatform extends AbstractOAuthPlatform {

    public YouTubeOAuthPlatform(WebClient webClient, SnsOAuthProperties snsOAuthProperties) {
        super(webClient, snsOAuthProperties);
    }

    @Override
    protected void addAuthUrlParameters(UriComponentsBuilder builder, SnsOAuthProperties.PlatformConfig config) {
        builder.queryParam("access_type", "offline");
    }

    @Override
    public Mono<UserInfo> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> log.error("YouTube 사용자 정보 조회 실패: {}", error.getMessage()))
                .flatMap(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items == null || items.isEmpty()){
                        return Mono.error(new SnsException(ErrorMessage.SNS_CONNECTION_FAILED));
                    }

                    Map<String, Object> item = items.get(0);
                    String id = (String) item.get("id");

                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    String customUrl = (String) snippet.get("customUrl");
                    String channelUrl = customUrl != null && customUrl.startsWith("@") ? 
                            "https://www.youtube.com/" + customUrl : 
                            "https://www.youtube.com/channel/" + id;

                    return Mono.just(new UserInfo(id, channelUrl));
                });
    }

    @Override
    public SnsPlatform getPlatform() {
        return SnsPlatform.YOUTUBE;
    }
} 