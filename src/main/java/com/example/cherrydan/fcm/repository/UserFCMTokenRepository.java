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
     * 사용자 ID로 활성화된 모든 디바이스 조회 (FCM 토큰이 있는 것만)
     * @param userId 사용자 ID
     * @return FCM 토큰이 있고 활성화된 디바이스 리스트
     */
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId = :userId AND t.fcmToken IS NOT NULL AND t.isActive = true")
    List<UserFCMToken> findActiveTokensByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 활성화된 모든 디바이스 조회 (FCM 토큰 유무와 관계없이)
     * @param userId 사용자 ID
     * @return 활성화된 모든 디바이스 리스트
     */
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId = :userId AND t.isActive = true")
    List<UserFCMToken> findActiveDevicesByUserId(@Param("userId") Long userId);
    
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
    @Query("SELECT t FROM UserFCMToken t WHERE t.userId IN :userIds AND t.fcmToken IS NOT NULL AND t.isActive = true")
    List<UserFCMToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}
