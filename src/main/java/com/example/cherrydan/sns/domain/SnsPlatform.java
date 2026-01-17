package com.example.cherrydan.sns.domain;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.SnsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnsPlatform {
    INSTAGRAM("인스타그램", "instagram"),
    YOUTUBE("유튜브", "youtube"),
    TIKTOK("틱톡", "tiktok"),
    NAVER("네이버", "naver");

    private final String displayName;
    private final String platformCode;

    /**
     * platformCode로부터 SnsPlatform을 찾습니다.
     * @param platformCode 플랫폼 코드
     * @return SnsPlatform
     * @throws SnsException 지원하지 않는 플랫폼인 경우
     */
    public static SnsPlatform fromPlatformCode(String platformCode) {
        if (platformCode == null || platformCode.trim().isEmpty()) {
            throw new SnsException(ErrorMessage.SNS_PLATFORM_CODE_EMPTY);
        }

        for (SnsPlatform platform : values()) {
            if (platform.platformCode.equalsIgnoreCase(platformCode.trim())) {
                return platform;
            }
        }

        throw new SnsException(ErrorMessage.SNS_PLATFORM_NOT_SUPPORTED);
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