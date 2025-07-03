package com.example.cherrydan.sns.dto;

import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SnsConnectionResponse {
    private final SnsPlatform platform;
    private final String snsUrl;
    private final boolean isConnected;

    public static SnsConnectionResponse from(SnsConnection connection) {
        return SnsConnectionResponse.builder()
                .platform(connection.getPlatform())
                .snsUrl(connection.getSnsUrl())
                .isConnected(true)
                .build();
    }

    public static SnsConnectionResponse notConnected(SnsPlatform platform) {
        return SnsConnectionResponse.builder()
                .platform(platform)
                .isConnected(false)
                .build();
    }
} 