package com.example.cherrydan.campaign.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_bookmark",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "campaign_id"})
    },
    indexes = {
        @Index(name = "idx_campaign_bookmark_user_active", columnList = "user_id, is_active")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bookmark extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void activate() {
        this.isActive = true;
    }
}