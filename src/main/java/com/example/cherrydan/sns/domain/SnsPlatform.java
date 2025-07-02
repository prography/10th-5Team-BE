package com.example.cherrydan.sns.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnsPlatform {
    INSTAGRAM("인스타그램", "instagram"),
    YOUTUBE("유튜브", "youtube"),
    TIKTOK("틱톡", "tiktok");

    private final String displayName;
    private final String platformCode;

    /**
     * platformCode로부터 SnsPlatform을 찾습니다.
     * @param platformCode 플랫폼 코드
     * @return SnsPlatform
     * @throws IllegalArgumentException 지원하지 않는 플랫폼인 경우
     */
    public static SnsPlatform fromPlatformCode(String platformCode) {
        if (platformCode == null || platformCode.trim().isEmpty()) {
            throw new IllegalArgumentException("플랫폼 코드가 비어있습니다.");
        }

        for (SnsPlatform platform : values()) {
            if (platform.platformCode.equalsIgnoreCase(platformCode.trim())) {
                return platform;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + platformCode);
    }

    /**
     * platformCode가 유효한지 확인합니다.
     * @param platformCode 플랫폼 코드
     * @return 유효 여부
     */
    public static boolean isValidPlatformCode(String platformCode) {
        if (platformCode == null || platformCode.trim().isEmpty()) {
            return false;
        }

        for (SnsPlatform platform : values()) {
            if (platform.platformCode.equalsIgnoreCase(platformCode.trim())) {
                return true;
            }
        }

        return false;
    }
} 