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
    private Integer alertStage = 0; // 0: 미발송, 1: 10개 알림 발송완료, 2: 100개 알림 발송완료

    @Column(name = "is_visible_to_user", nullable = false)
    @Builder.Default
    private Boolean isVisibleToUser = true;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false; // 읽음 상태


    public void markAsNotified() {
        // 발송 완료 상태로 변경 (간단!)
        this.alertStage = 1;
    }

    public boolean isNotified() {
        return alertStage > 0;
    }

    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 숨김 처리 (소프트 삭제)
     */
    public void hide() {
        this.isVisibleToUser = false;
    }
} 