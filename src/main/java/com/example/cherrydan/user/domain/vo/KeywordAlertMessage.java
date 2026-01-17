package com.example.cherrydan.user.domain.vo;

import com.example.cherrydan.notification.domain.AlertMessage;

import java.util.Map;

/**
 * 키워드 알림 메시지 Value Object
 */
public record KeywordAlertMessage(
    String title,
    String body,
    Map<String, String> data
) implements AlertMessage {
    private static final String DEFAULT_TITLE = "체리단";
    private static final String MESSAGE_TEMPLATE = "'%s' 키워드 캠페인이 %s건 등록됐어요.";
    private static final String TYPE = "keyword_campaign";
    private static final String ACTION = "open_personalized_page";

    /**
     * 키워드와 캠페인 수로부터 알림 메시지 생성
     *
     * @param keyword 키워드
     * @param campaignCount 캠페인 수
     * @param policy 알림 정책
     * @return 알림 메시지
     */
    public static KeywordAlertMessage create(
        String keyword,
        int campaignCount,
        KeywordAlertPolicy policy
    ) {
        String countText = policy.formatCountText(campaignCount);
        String body = String.format(MESSAGE_TEMPLATE, keyword, countText);

        Map<String, String> data = Map.of(
            "type", TYPE,
            "keyword", keyword,
            "dailyNewCount", String.valueOf(campaignCount),
            "action", ACTION
        );

        return new KeywordAlertMessage(DEFAULT_TITLE, body, data);
    }
}
