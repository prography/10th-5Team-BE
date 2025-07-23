package com.example.cherrydan.fcm.repository;

import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 Repository
 * 사용자 FCM 토큰 관련 데이터베이스 접근을 담당하는 인터페이스
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
@Repository
public interface UserFCMTokenRepository extends JpaRepository<UserFCMToken, Long> {
    
    /**
     * 사용자 ID로 활성화된 모든 토큰 조회
     * @param userId 사용자 ID
     * @return 활성화된 FCM 토큰 리스트
     */
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId = :userId AND t.isActive = true")
    List<UserFCMToken> findActiveTokensByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID와 디바이스 타입으로 토큰 조회
     * @param userId 사용자 ID
     * @param deviceType 디바이스 타입
     * @return FCM 토큰 (Optional)
     */
    Optional<UserFCMToken> findByUserIdAndDeviceTypeAndIsActiveTrue(Long userId, DeviceType deviceType);
    
    /**
     * FCM 토큰으로 조회
     * @param fcmToken FCM 토큰
     * @return FCM 토큰 엔티티 (Optional)
     */
    Optional<UserFCMToken> findByFcmToken(String fcmToken);
    
    /**
     * 여러 사용자 ID로 활성화된 토큰들 조회
     * @param userIds 사용자 ID 리스트
     * @return 활성화된 FCM 토큰 리스트
     */
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId IN :userIds AND t.isActive = true")
    List<UserFCMToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    
    /**
     * 마지막 사용 시간이 특정 기간 이전인 토큰들 조회 (정리용)
     * @param dateTime 기준 시간
     * @return 오래된 FCM 토큰 리스트
     */
    @Query("SELECT t FROM UserFCMToken t WHERE t.lastUsedAt < :dateTime AND t.isActive = true")
    List<UserFCMToken> findTokensNotUsedSince(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * 특정 FCM 토큰 비활성화
     * @param fcmToken FCM 토큰
     */
    @Modifying
    @Query("UPDATE UserFCMToken t SET t.isActive = false WHERE t.fcmToken = :fcmToken")
    void deactivateByFcmToken(@Param("fcmToken") String fcmToken);
    
    /**
     * 사용자의 모든 토큰 비활성화
     * @param userId 사용자 ID
     */
    @Modifying
    @Query("UPDATE UserFCMToken t SET t.isActive = false WHERE t.userId = :userId")
    void deactivateAllTokensByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 토큰 존재 여부 확인
     * @param userId 사용자 ID
     * @return 토큰 존재 여부
     */
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}
