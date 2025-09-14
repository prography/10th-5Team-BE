package com.example.cherrydan.activity.domain;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "activity_alerts",
    indexes = {
        @Index(name = "idx_user_visible_alert_date", columnList = "user_id, is_visible_to_user, alert_date"),
        @Index(name = "idx_alert_stage_visible", columnList = "alert_stage, is_visible_to_user")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_activity_alert", columnNames = {"user_id", "campaign_id", "alert_type", "alert_date"})
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityAlert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private ActivityAlertType alertType;

    @Column(name = "alert_stage", nullable = false)
    @Builder.Default
    private Integer alertStage = 0; // 0: 미발송, 1: 발송완료

    @Column(name = "is_visible_to_user", nullable = false)
    @Builder.Default
    private Boolean isVisibleToUser = true;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    public void markAsNotified() {
        this.alertStage = 1;
    }

    public void markAsRead() {
        this.isRead = true;
    }
    
    public String getNotificationTitle() {
        return alertType.getTitle();
    }
    
    public String getNotificationBody() {
        return alertType.getBodyTemplate(campaign.getTitle());
    }
}