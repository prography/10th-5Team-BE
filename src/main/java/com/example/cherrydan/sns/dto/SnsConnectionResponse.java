package com.example.cherrydan.sns.dto;

import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SnsConnectionResponse {
    @Schema(description = "플랫폼 (유튜브, 인스타 등)", requiredMode = Schema.RequiredMode.REQUIRED)
    private final SnsPlatform platform;
    @Schema(description = "유저의 연결된 sns 링크", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String snsUrl;
    @Schema(description = "유저의 특정 플랫폼 연결 유무", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Boolean isConnected;

    public static SnsConnectionResponse from(SnsConnection connection) {
        return SnsConnectionResponse.builder()
                .platform(connection.getPlatform())
                .snsUrl(connection.getSnsUrl())
                .isConnected(true)
                .build();
    }
} 