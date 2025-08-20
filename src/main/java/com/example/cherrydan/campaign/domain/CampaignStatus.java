package com.example.cherrydan.campaign.domain;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "campaign_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatus extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.ORDINAL)
    private CampaignStatusType status;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true; // 이건 캠페인이 종료되면 false로 할 듯
    
    /**
     * 활동 알림 발송 여부
     */
    @Builder.Default
    @Column(name = "activity_notified")
    private Boolean activityNotified = false;

    /**
     * 활동 알림 발송 시각
     */
    @Column(name = "activity_notified_at")
    private LocalDateTime activityNotifiedAt;

    /**
     * 활동 알림 읽음 처리 여부
     */
    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Builder.Default
    @Column(name = "is_visible_to_user", nullable = false)
    private Boolean isVisibleToUser = true;
    
    /**
     * 활동 알림 대상인지 확인 (3일 이내 마감)
     */
    public boolean isActivityEligible() {
        Integer daysRemaining = getDaysRemaining();
        return daysRemaining != null && daysRemaining >= 0 && daysRemaining <= 3;
    }
    
    /**
     * 활동 마감까지 남은 일수 계산
     */
    public Integer getDaysRemaining() {
        LocalDate targetDate = getActivityTargetDate();
        if (targetDate == null) return null;
        
        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return (int) days;
    }
    
    /**
     * 활동 목표 날짜 계산 (상태에 따라)
     */
    public LocalDate getActivityTargetDate() {
        if (campaign == null) return null;
        
        switch (this.status) {
            case APPLY:
                return campaign.getReviewerAnnouncement();
            case SELECTED:
            case REVIEWING:
                return campaign.getContentSubmissionEnd();
            default:
                return null;
        }
    }
    
    /**
     * 활동 알림 발송 완료 표시
     */
    public void markActivityAsNotified() {
        this.activityNotified = true;
        this.activityNotifiedAt = LocalDateTime.now();
    }
    
    /**
     * 활동 알림 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
} 