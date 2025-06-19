package com.example.cherrydan.push.dto;

import lombok.Getter;

@Getter
public enum PushCategory {
    ACTIVITY("활동 알림 (공고 마감, 선정결과 등)"),
    PERSONALIZED("맞춤 알림 (키워드 알림 등)"),
    SERVICE("서비스 알림 (안내공지, 1:1 문의 답변)"),
    MARKETING("마케팅 알림 (이벤트, 프로모션)");

    private final String description;

    PushCategory(String description) {
        this.description = description;
    }
}
