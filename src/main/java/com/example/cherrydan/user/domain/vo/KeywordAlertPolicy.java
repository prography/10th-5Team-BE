package com.example.cherrydan.user.domain.vo;

import org.springframework.util.Assert;

import static org.springframework.util.Assert.*;

/**
 * 키워드 알림 정책 Value Object
 */
public record KeywordAlertPolicy(int highThreshold, int lowThreshold) {

    public static final KeywordAlertPolicy DEFAULT = new KeywordAlertPolicy(100, 10);

    public KeywordAlertPolicy {
        state(highThreshold > lowThreshold, "상위 임계값은 하위 임계값보다 커야 합니다");
        state(lowThreshold > 0, "임계값은 0보다 커야 합니다");
    }

    /**
     * 캠페인 수를 범위별 텍스트로 변환
     *
     * @param count 캠페인 수
     * @return 포맷된 텍스트 (예: "10+", "100+", "5")
     */
    public String formatCountText(int count) {
        if (count >= highThreshold) {
            return highThreshold + "+";
        }
        if (count >= lowThreshold) {
            return lowThreshold + "+";
        }
        return String.valueOf(count);
    }

    /**
     * 캠페인 수에 따른 알림 단계 결정
     *
     * @param count 캠페인 수
     * @return 알림 단계 (0: 미발송, 1: 10+건 발송, 2: 100+건 발송)
     */
    public int determineAlertStage(int count) {
        if (count >= highThreshold) return 2;
        if (count >= lowThreshold) return 1;
        return 0;
    }

    /**
     * 특정 단계의 알림을 발송해야 하는지 확인
     *
     * @param count 캠페인 수
     * @param currentStage 현재 알림 단계
     * @return 발송 필요 여부
     */
    public boolean shouldNotify(int count, int currentStage) {
        int requiredStage = determineAlertStage(count);
        return requiredStage > currentStage;
    }
}
