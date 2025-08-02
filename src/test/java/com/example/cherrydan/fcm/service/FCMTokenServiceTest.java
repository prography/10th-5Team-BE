package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.ErrorMessage;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FCMTokenService 단위 테스트")
class FCMTokenServiceTest {

    @Mock
    private UserFCMTokenRepository tokenRepository;

    @InjectMocks
    private FCMTokenService fcmTokenService;

    private FCMTokenRequest validRequest;
    private UserFCMToken existingToken;

    @BeforeEach
    void setUp() {
        validRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("valid_fcm_token")
                .deviceType("android")
                .deviceModel("Galaxy S23")
                .appVersion("1.0.0")
                .osVersion("Android 13")
                .build();

        existingToken = UserFCMToken.builder()
                .id(1L)
                .userId(1L)
                .fcmToken("old_fcm_token")
                .deviceType(DeviceType.ANDROID)
                .deviceModel("Galaxy S22")
                .appVersion("0.9.0")
                .osVersion("Android 12")
                .isActive(true)
                .lastUsedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("새 FCM 토큰 등록 성공")
    void registerNewToken_Success() {
        // given
        given(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID))
                .willReturn(Optional.empty());
        
        UserFCMToken savedToken = UserFCMToken.builder()
                .id(1L)
                .userId(1L)
                .fcmToken("valid_fcm_token")
                .deviceType(DeviceType.ANDROID)
                .deviceModel("Galaxy S23")
                .appVersion("1.0.0")
                .osVersion("Android 13")
                .isActive(true)
                .build();
        
        given(tokenRepository.save(any(UserFCMToken.class))).willReturn(savedToken);

        // when
        Long result = fcmTokenService.registerOrUpdateToken(validRequest);

        // then
        assertThat(result).isEqualTo(1L);
        verify(tokenRepository).findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID);
        verify(tokenRepository).save(any(UserFCMToken.class));
    }

    @Test
    @DisplayName("기존 FCM 토큰 업데이트 성공")
    void updateExistingToken_Success() {
        // given
        given(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID))
                .willReturn(Optional.of(existingToken));

        // when
        Long result = fcmTokenService.registerOrUpdateToken(validRequest);

        // then
        assertThat(result).isEqualTo(1L);
        verify(tokenRepository).findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID);
        verify(tokenRepository, never()).save(any(UserFCMToken.class));
        
        // existingToken의 상태 변화 검증
        assertThat(existingToken.getFcmToken()).isEqualTo("valid_fcm_token");
        assertThat(existingToken.getDeviceModel()).isEqualTo("Galaxy S23");
        assertThat(existingToken.getAppVersion()).isEqualTo("1.0.0");
        assertThat(existingToken.getOsVersion()).isEqualTo("Android 13");
        assertThat(existingToken.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 요청 - userId null")
    void registerToken_InvalidRequest_UserIdNull() {
        // given
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(null)
                .fcmToken("valid_fcm_token")
                .deviceType("android")
                .build();

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(invalidRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_TOKEN_INVALID_REQUEST.getMessage());
        
        verify(tokenRepository, never()).findByUserIdAndDeviceTypeAndIsActiveTrue(any(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("유효하지 않은 요청 - fcmToken null")
    void registerToken_InvalidRequest_FcmTokenNull() {
        // given
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken(null)
                .deviceType("android")
                .build();

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(invalidRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_TOKEN_INVALID_REQUEST.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 요청 - fcmToken 빈 문자열")
    void registerToken_InvalidRequest_FcmTokenEmpty() {
        // given
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("   ")
                .deviceType("android")
                .build();

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(invalidRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_TOKEN_INVALID_REQUEST.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 요청 - deviceType null")
    void registerToken_InvalidRequest_DeviceTypeNull() {
        // given
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("valid_fcm_token")
                .deviceType(null)
                .build();

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(invalidRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_TOKEN_INVALID_REQUEST.getMessage());
    }

    @Test
    @DisplayName("잘못된 디바이스 타입")
    void registerToken_InvalidDeviceType() {
        // given
        FCMTokenRequest invalidRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("valid_fcm_token")
                .deviceType("invalid_device_type")
                .build();

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(invalidRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_DEVICE_TYPE_INVALID.getMessage());
    }

    @Test
    @DisplayName("Repository 예외 발생 시 FCMException 변환")
    void registerToken_RepositoryException() {
        // given
        given(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID))
                .willThrow(new RuntimeException("Database connection failed"));

        // when & then
        assertThatThrownBy(() -> fcmTokenService.registerOrUpdateToken(validRequest))
                .isInstanceOf(FCMException.class)
                .hasMessage(ErrorMessage.FCM_TOKEN_REGISTRATION_FAILED.getMessage());
    }

    @Test
    @DisplayName("부분적 디바이스 정보 업데이트")
    void updateToken_PartialDeviceInfo() {
        // given
        FCMTokenRequest partialRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("new_fcm_token")
                .deviceType("android")
                .deviceModel("Galaxy S24")  // 이것만 업데이트
                .appVersion(null)           // null은 업데이트 안됨
                .osVersion("")              // 빈 문자열도 업데이트 안됨
                .build();

        given(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.ANDROID))
                .willReturn(Optional.of(existingToken));

        // when
        Long result = fcmTokenService.registerOrUpdateToken(partialRequest);

        // then
        assertThat(result).isEqualTo(1L);
        assertThat(existingToken.getFcmToken()).isEqualTo("new_fcm_token");
        assertThat(existingToken.getDeviceModel()).isEqualTo("Galaxy S24");  // 업데이트됨
        assertThat(existingToken.getAppVersion()).isEqualTo("0.9.0");       // 기존 값 유지
        assertThat(existingToken.getOsVersion()).isEqualTo("Android 12");   // 기존 값 유지
    }

    @Test
    @DisplayName("iOS 디바이스 타입 처리")
    void registerToken_IOSDeviceType() {
        // given
        FCMTokenRequest iosRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("ios_fcm_token")
                .deviceType("ios")
                .deviceModel("iPhone 14 Pro")
                .appVersion("1.0.0")
                .osVersion("iOS 16.5")
                .build();

        given(tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.IOS))
                .willReturn(Optional.empty());
        
        UserFCMToken savedToken = UserFCMToken.builder()
                .id(2L)
                .userId(1L)
                .fcmToken("ios_fcm_token")
                .deviceType(DeviceType.IOS)
                .deviceModel("iPhone 14 Pro")
                .appVersion("1.0.0")
                .osVersion("iOS 16.5")
                .isActive(true)
                .build();
        
        given(tokenRepository.save(any(UserFCMToken.class))).willReturn(savedToken);

        // when
        Long result = fcmTokenService.registerOrUpdateToken(iosRequest);

        // then
        assertThat(result).isEqualTo(2L);
        verify(tokenRepository).findByUserIdAndDeviceTypeAndIsActiveTrue(1L, DeviceType.IOS);
        verify(tokenRepository).save(any(UserFCMToken.class));
    }
}