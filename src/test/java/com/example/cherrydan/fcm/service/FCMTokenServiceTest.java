package com.example.cherrydan.fcm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.cherrydan.common.exception.FCMException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("FCM 토큰 서비스 테스트")
class FCMTokenServiceTest {

    @Mock
    private UserFCMTokenRepository tokenRepository;
    
    @InjectMocks
    private FCMTokenService fcmTokenService;

    private Long testUserId;
    private String testFcmToken;
    private UserFCMToken testTokenEntity;

    @BeforeEach
    void setUp() {
        testUserId = 123L;  // Long으로 변경
        testFcmToken = generateMockFcmToken();
        
        testTokenEntity = UserFCMToken.builder()
                .id(1L)
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType(DeviceType.ANDROID)
                .isActive(true)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("새 FCM 토큰 등록 성공")
    void registerOrUpdateToken_NewToken_Success() {
        // Given
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType("android")
                .build();

        when(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(testUserId, DeviceType.ANDROID))
                .thenReturn(Optional.empty());
        when(tokenRepository.save(any(UserFCMToken.class)))
                .thenReturn(testTokenEntity);

        // When
        Long result = fcmTokenService.registerOrUpdateToken(request);

        // Then
        assertEquals(1L, result);
        verify(tokenRepository).save(argThat(entity -> 
                entity.getUserId().equals(testUserId) &&
                entity.getFcmToken().equals(testFcmToken) &&
                entity.getDeviceType() == DeviceType.ANDROID
        ));
    }

    @Test
    @DisplayName("기존 FCM 토큰 업데이트 성공")
    void registerOrUpdateToken_ExistingToken_Success() {
        // Given
        String newFcmToken = generateMockFcmToken();
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(newFcmToken)
                .deviceType("android")
                .build();

        when(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(testUserId, DeviceType.ANDROID))
                .thenReturn(Optional.of(testTokenEntity));

        // When
        Long result = fcmTokenService.registerOrUpdateToken(request);

        // Then
        assertEquals(1L, result);
        // testTokenEntity.updateToken(newFcmToken)이 호출되었는지는 실제 엔티티에서 확인
        verify(tokenRepository, never()).save(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("iOS 토큰 등록 성공")
    void registerOrUpdateToken_iOSDevice_Success() {
        // Given
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType("ios")
                .build();

        when(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(testUserId, DeviceType.IOS))
                .thenReturn(Optional.empty());
        
        UserFCMToken iosToken = UserFCMToken.builder()
                .id(2L)
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType(DeviceType.IOS)
                .isActive(true)
                .build();
        
        when(tokenRepository.save(any(UserFCMToken.class)))
                .thenReturn(iosToken);

        // When
        Long result = fcmTokenService.registerOrUpdateToken(request);

        // Then
        assertEquals(2L, result);
        verify(tokenRepository).save(argThat(entity -> 
                entity.getDeviceType() == DeviceType.IOS
        ));
    }

    @Test
    @DisplayName("잘못된 디바이스 타입으로 등록 시도 시 예외 발생")
    void registerOrUpdateToken_InvalidDeviceType_ThrowsException() {
        // Given
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType("invalid-device")
                .build();

        // When & Then
        assertThrows(FCMException.class, () -> {
            fcmTokenService.registerOrUpdateToken(request);
        });
        
        verify(tokenRepository, never()).save(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 요청으로 예외 발생")
    void registerOrUpdateToken_InvalidRequest_ThrowsException() {
        // Given - userId가 null인 경우
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(null)
                .fcmToken(testFcmToken)
                .deviceType("android")
                .build();

        // When & Then
        assertThrows(FCMException.class, () -> {
            fcmTokenService.registerOrUpdateToken(invalidRequest);
        });
        
        verify(tokenRepository, never()).save(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("빈 FCM 토큰으로 등록 시도 시 예외 발생")
    void registerOrUpdateToken_EmptyToken_ThrowsException() {
        // Given
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken("")
                .deviceType("android")
                .build();

        // When & Then
        assertThrows(FCMException.class, () -> {
            fcmTokenService.registerOrUpdateToken(request);
        });
    }

    @Test
    @DisplayName("사용자 토큰 조회 성공")
    void getUserTokens_Success() {
        // Given
        UserFCMToken iosToken = UserFCMToken.builder()
                .id(2L)
                .userId(testUserId)
                .fcmToken(generateMockFcmToken())
                .deviceType(DeviceType.IOS)
                .isActive(true)
                .build();

        when(tokenRepository.findActiveTokensByUserId(testUserId))
                .thenReturn(List.of(testTokenEntity, iosToken));

        // When
        List<UserFCMToken> userTokens = fcmTokenService.getUserTokens(testUserId);

        // Then
        assertEquals(2, userTokens.size());
        assertTrue(userTokens.stream().allMatch(token -> 
                token.getUserId().equals(testUserId) && token.getIsActive()));
        
        assertTrue(userTokens.stream().anyMatch(token -> 
                token.getDeviceType() == DeviceType.ANDROID));
        assertTrue(userTokens.stream().anyMatch(token -> 
                token.getDeviceType() == DeviceType.IOS));
    }

    @Test
    @DisplayName("FCM 토큰 삭제 성공")
    void deleteToken_Success() {
        // Given
        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.of(testTokenEntity));

        // When
        fcmTokenService.deleteToken(testUserId, testFcmToken);

        // Then
        verify(tokenRepository).delete(testTokenEntity);
    }

    @Test
    @DisplayName("존재하지 않는 토큰 삭제 시도 시 예외 발생")
    void deleteToken_TokenNotFound_ThrowsException() {
        // Given
        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(FCMException.class, () -> {
            fcmTokenService.deleteToken(testUserId, testFcmToken);
        });
        
        verify(tokenRepository, never()).delete(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("다른 사용자의 토큰 삭제 시도 시 예외 발생")
    void deleteToken_AccessDenied_ThrowsException() {
        // Given
        Long differentUserId = 999L;  // Long으로 변경
        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.of(testTokenEntity));

        // When & Then
        assertThrows(FCMException.class, () -> {
            fcmTokenService.deleteToken(differentUserId, testFcmToken);
        });
        
        verify(tokenRepository, never()).delete(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("사용자의 모든 토큰 삭제 성공")
    void deleteAllUserTokens_Success() {
        // When
        fcmTokenService.deleteAllUserTokens(testUserId);

        // Then
        verify(tokenRepository).deactivateAllTokensByUserId(testUserId);
    }

    @Test
    @DisplayName("토큰 유효성 검증 성공")
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Given
        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.of(testTokenEntity));

        // When
        boolean isValid = fcmTokenService.isTokenValid(testFcmToken);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("토큰 유효성 검증 실패 - 토큰 없음")
    void isTokenValid_TokenNotFound_ReturnsFalse() {
        // Given
        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.empty());

        // When
        boolean isValid = fcmTokenService.isTokenValid(testFcmToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("토큰 유효성 검증 실패 - 비활성화된 토큰")
    void isTokenValid_InactiveToken_ReturnsFalse() {
        // Given
        UserFCMToken inactiveToken = UserFCMToken.builder()
                .id(1L)
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType(DeviceType.ANDROID)
                .isActive(false) // 비활성화
                .build();

        when(tokenRepository.findByFcmToken(testFcmToken))
                .thenReturn(Optional.of(inactiveToken));

        // When
        boolean isValid = fcmTokenService.isTokenValid(testFcmToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("사용자 활성 토큰 존재 여부 확인 - 존재함")
    void hasActiveTokens_TokensExist_ReturnsTrue() {
        // Given
        when(tokenRepository.existsByUserIdAndIsActiveTrue(testUserId))
                .thenReturn(true);

        // When
        boolean hasTokens = fcmTokenService.hasActiveTokens(testUserId);

        // Then
        assertTrue(hasTokens);
    }

    @Test
    @DisplayName("사용자 활성 토큰 존재 여부 확인 - 존재하지 않음")
    void hasActiveTokens_NoTokens_ReturnsFalse() {
        // Given
        when(tokenRepository.existsByUserIdAndIsActiveTrue(testUserId))
                .thenReturn(false);

        // When
        boolean hasTokens = fcmTokenService.hasActiveTokens(testUserId);

        // Then
        assertFalse(hasTokens);
    }

    @Test
    @DisplayName("오래된 토큰 정리 성공")
    void cleanupOldTokens_Success() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        
        UserFCMToken oldToken1 = UserFCMToken.builder()
                .id(10L)
                .userId(100L)  // Long으로 변경
                .fcmToken(generateMockFcmToken())
                .deviceType(DeviceType.ANDROID)
                .isActive(true)
                .lastUsedAt(cutoffDate.minusDays(10))
                .build();

        UserFCMToken oldToken2 = UserFCMToken.builder()
                .id(11L)
                .userId(200L)  // Long으로 변경
                .fcmToken(generateMockFcmToken())
                .deviceType(DeviceType.IOS)
                .isActive(true)
                .lastUsedAt(cutoffDate.minusDays(5))
                .build();

        when(tokenRepository.findTokensNotUsedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(oldToken1, oldToken2));

        // When
        fcmTokenService.cleanupOldTokens();

        // Then
        verify(tokenRepository).findTokensNotUsedSince(any(LocalDateTime.class));
        verify(tokenRepository).deleteAll(List.of(oldToken1, oldToken2));
    }

    @Test
    @DisplayName("동일 사용자 다른 디바이스 토큰 등록")
    void registerOrUpdateToken_SameUserDifferentDevices_Success() {
        // Given - Android 토큰 먼저 등록
        FCMTokenRequest androidRequest = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(testFcmToken)
                .deviceType("android")
                .build();

        when(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(testUserId, DeviceType.ANDROID))
                .thenReturn(Optional.empty());
        when(tokenRepository.save(any(UserFCMToken.class)))
                .thenReturn(testTokenEntity);

        // When - Android 토큰 등록
        Long androidResult = fcmTokenService.registerOrUpdateToken(androidRequest);

        // Given - iOS 토큰 등록
        String iosToken = generateMockFcmToken();
        FCMTokenRequest iosRequest = FCMTokenRequest.builder()
                .userId(testUserId)
                .fcmToken(iosToken)
                .deviceType("ios")
                .build();
        
        UserFCMToken iosTokenEntity = UserFCMToken.builder()
                .id(2L)
                .userId(testUserId)
                .fcmToken(iosToken)
                .deviceType(DeviceType.IOS)
                .isActive(true)
                .build();

        when(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(testUserId, DeviceType.IOS))
                .thenReturn(Optional.empty());
        when(tokenRepository.save(any(UserFCMToken.class)))
                .thenReturn(iosTokenEntity);

        // When - iOS 토큰 등록
        Long iosResult = fcmTokenService.registerOrUpdateToken(iosRequest);

        // Then
        assertEquals(1L, androidResult);
        assertEquals(2L, iosResult);
        
        // 두 번의 save 호출 확인 (Android, iOS 각각)
        verify(tokenRepository, times(2)).save(any(UserFCMToken.class));
    }

    /**
     * 실제와 유사한 FCM 토큰을 모킹으로 생성
     */
    private String generateMockFcmToken() {
        String prefix = "dA3kL9mN2xR8qP5tY9wZ";
        String randomSuffix = UUID.randomUUID().toString().replace("-", "");
        return prefix + randomSuffix + "x7vM4nC8bF1hJ6kG0uY3sW2eT5rI9oP";
    }

    /**
     * Long userId로 토큰 엔티티 생성 헬퍼
     */
    private UserFCMToken createToken(Long userId, String token, DeviceType deviceType) {
        return UserFCMToken.builder()
                .id(System.currentTimeMillis()) // 간단한 ID 생성
                .userId(userId)
                .fcmToken(token)
                .deviceType(deviceType)
                .isActive(true)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }
}
