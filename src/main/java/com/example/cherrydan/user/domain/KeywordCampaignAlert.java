package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "keyword_campaign_alerts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordCampaignAlert extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword; // 매칭된 키워드

    @Column(name = "campaign_count", nullable = false)
    private Integer campaignCount; // 매칭된 캠페인 수

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate; // 알림 날짜

    @Column(name = "alert_stage", nullable = false)
    @Builder.Default
    private Integer alertStage = 0; // 0: 미알림, 1: 10개 알림 완료, 2: 100개 알림 완료

    @Column(name = "is_notified", nullable = false)
    @Builder.Default
    private Boolean isNotified = false; // 푸시 알림 발송 여부

    @Column(name = "is_visible_to_user", nullable = false)
    @Builder.Default
    private Boolean isVisibleToUser = true;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false; // 읽음 상태

    public void markAsNotified() {
        this.isNotified = true;
    }

    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 알림 정보 업데이트
     */
    public void updateAlertInfo(int campaignCount, LocalDate alertDate) {
        this.campaignCount = campaignCount;
        this.alertDate = alertDate;
        this.isNotified = false; // 알림 발송 상태 초기화
        updateAlertStage(campaignCount);
    }

    /**
     * 알림 단계 업데이트
     */
    public void updateAlertStage(int campaignCount) {
        if (campaignCount >= 100) {
            this.alertStage = 2; // 100개 알림 완료
        } else if (campaignCount >= 10) {
            this.alertStage = 1; // 10개 알림 완료
        }
    }
} 