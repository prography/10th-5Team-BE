package com.example.cherrydan.activity.domain;

import lombok.Getter;

@Getter
public enum ActivityAlertType {
    // 북마크 기반 알림
    BOOKMARK_DEADLINE_D1("마감알림", "D-1", "모집이 내일 완료됩니다. 얼른 신청해보세요."),
    BOOKMARK_DEADLINE_DDAY("마감알림", "D-Day", "모집이 오늘 종료됩니다."),
    
    // CampaignStatus 기반 알림
    APPLY_RESULT_DDAY("공고 결과 알림", "D-Day", "선정 결과를 확인해보세요!"),
    
    // 선정자 방문 알림 (REGION 타입만)
    SELECTED_VISIT_D3("방문알림", "D-3", "방문 마감일이 3일 남았습니다."),
    SELECTED_VISIT_DDAY("방문알림", "D-Day", "오늘이 마지막 방문 기회예요!"),
    
    // 리뷰 작성 알림
    REVIEWING_DEADLINE_D3("리뷰 작성 알림", "D-3", "리뷰 작성이 3일 남았습니다."),
    REVIEWING_DEADLINE_DDAY("리뷰 작성 알림", "D-Day", "리뷰 작성이 오늘 마감됩니다. 놓치지 말고 작성해주세요.");
    
    private final String category;
    private final String dDayLabel;
    private final String messageTemplate;
    
    ActivityAlertType(String category, String dDayLabel, String messageTemplate) {
        this.category = category;
        this.dDayLabel = dDayLabel;
        this.messageTemplate = messageTemplate;
    }
    
    public String getTitle() {
        return category;
    }
    
    public String getBodyTemplate(String campaignTitle) {
        return String.format("%s %s %s", dDayLabel, campaignTitle, messageTemplate);
    }
}