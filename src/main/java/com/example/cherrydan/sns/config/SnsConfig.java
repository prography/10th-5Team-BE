package com.example.cherrydan.sns.config;

import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.service.OAuthPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SnsConfig {
    // 블로그 인증 방식으로 변경되어 WebClient가 더 이상 필요하지 않음

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean
    public Map<SnsPlatform, OAuthPlatform> oauthPlatforms(List<OAuthPlatform> platforms) {
        return platforms.stream()
                .collect(Collectors.toMap(
                        OAuthPlatform::getPlatform,
                        platform -> platform
                ));
    }
} 