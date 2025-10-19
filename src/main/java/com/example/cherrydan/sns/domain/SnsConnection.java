package com.example.cherrydan.sns.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sns_connections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "platform"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
public class SnsConnection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SnsPlatform platform;

    @Column(unique = true)
    private String snsUserId;

    private String snsUrl;

    @Column(name = "platform_user_id")
    private String platformUserId;

    @Column(name = "platform_username")
    private String platformUsername;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void updateSnsInfo(String snsUserId, String snsUrl) {
        this.snsUserId = snsUserId;
        this.snsUrl = snsUrl;
    }

    public void setPlatformUserId(String platformUserId) {
        this.platformUserId = platformUserId;
    }

    public void setPlatformUsername(String platformUsername) {
        this.platformUsername = platformUsername;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }
} 