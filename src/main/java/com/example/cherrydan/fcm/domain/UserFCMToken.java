package com.example.cherrydan.fcm.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 FCM 토큰 엔티티
 * 사용자의 디바이스별 FCM 토큰을 저장하는 엔티티
 *
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
@Entity
@Table(
    name = "user_fcm_tokens",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_user_device", columnList = "user_id, device_type"),
        @Index(name = "idx_fcm_token", columnList = "fcm_token")
    }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFCMToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * FCM 토큰 (디바이스별로 고유)
     */
    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    /**
     * 디바이스 타입 (android, ios)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 10)
    private DeviceType deviceType;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "os_version")
    private String osVersion;


    /**
     * 토큰 활성화 상태
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 마지막 사용 시간 (알림 전송 시 업데이트)
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * FCM 토큰 업데이트
     * @param newToken 새로운 FCM 토큰
     */
    public void updateToken(String newToken) {
        this.fcmToken = newToken;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * 토큰 비활성화
     * 무효한 토큰인 경우 비활성화 처리
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 토큰 활성화
     */
    public void activate() {
        this.isActive = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * 마지막 사용 시간 업데이트
     * 알림 전송 성공 시 호출
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
