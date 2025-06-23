package com.example.cherrydan.sns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * SNS OAuth 설정 프로퍼티
 * 각 플랫폼별 OAuth 설정을 관리합니다.
 */
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "sns.oauth")
public class SnsOAuthProperties {
    
    @Valid
    @NotNull(message = "플랫폼 설정이 필요합니다.")
    private Map<String, PlatformConfig> platforms;
    
    /**
     * 플랫폼별 설정을 가져옵니다.
     * @param platformCode 플랫폼 코드
     * @return 플랫폼 설정
     */
    public PlatformConfig getPlatformConfig(String platformCode) {
        PlatformConfig config = platforms.get(platformCode);
        if (config == null) {
            throw new IllegalArgumentException("플랫폼 설정을 찾을 수 없습니다: " + platformCode);
        }
        return config;
    }
    
    /**
     * 플랫폼 설정이 존재하는지 확인합니다.
     * @param platformCode 플랫폼 코드
     * @return 존재 여부
     */
    public boolean hasPlatformConfig(String platformCode) {
        return platforms.containsKey(platformCode);
    }
    
    /**
     * 플랫폼별 OAuth 설정
     */
    @Getter
    @Setter
    public static class PlatformConfig {
        
        @NotBlank(message = "클라이언트 ID는 필수입니다.")
        private String clientId;
        
        @NotBlank(message = "클라이언트 시크릿은 필수입니다.")
        private String clientSecret;
        
        @NotBlank(message = "리다이렉트 URI는 필수입니다.")
        private String redirectUri;
        
        @NotBlank(message = "인증 URL은 필수입니다.")
        private String authUrl;
        
        @NotBlank(message = "토큰 URL은 필수입니다.")
        private String tokenUrl;
        
        private String userInfoUrl;
        
        @NotBlank(message = "스코프는 필수입니다.")
        private String scope;
    }
} 