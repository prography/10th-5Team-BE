package com.example.cherrydan.common.constants;

/**
 * 딥링크 관련 상수 및 유틸리티 클래스
 */
public class DeepLinkConstants {

    private static final String SCHEME = "cherrydan";
    private static final String OAUTH_CALLBACK_PATH = "oauth/callback";

    private DeepLinkConstants() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스화할 수 없습니다");
    }

    /**
     * OAuth 성공 딥링크 URL 생성
     * @param platform 플랫폼 코드
     * @return 딥링크 URL
     */
    public static String buildOAuthSuccessUrl(String platform) {
        return String.format("%s://%s?success=true&platform=%s",
                SCHEME, OAUTH_CALLBACK_PATH, platform);
    }

    /**
     * OAuth 실패 딥링크 URL 생성
     * @param errorCode 에러 코드
     * @return 딥링크 URL
     */
    public static String buildOAuthFailureUrl(String errorCode) {
        return String.format("%s://%s?success=false&error=%s",
                SCHEME, OAUTH_CALLBACK_PATH, errorCode);
    }
}