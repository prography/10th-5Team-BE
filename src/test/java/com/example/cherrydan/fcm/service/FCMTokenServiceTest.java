package com.example.cherrydan.fcm.service;

import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("FCMTokenService 통합테스트")
class FCMTokenServiceTest {

    @Autowired
    private UserFCMTokenRepository tokenRepository;

    @Autowired
    private FCMTokenService fcmTokenService;

    private FCMTokenRequest fcmTokenRequest;

    @BeforeEach
    void setUp() {
        fcmTokenRequest = FCMTokenRequest.builder()
                .userId(1L)
                .fcmToken("test-fcm-token")
                .deviceType("android")
                .deviceModel("Galaxy S23")
                .appVersion("1.0.0")
                .osVersion("13")
                .build();
    }

    @Nested
    @DisplayName("FCM 토큰 등록/업데이트")
    class RegisterOrUpdateToken {

        @Test
        @DisplayName("새로운 FCM 토큰이 성공적으로 등록된다")
        void registerNewToken_Success() {
            // when
            fcmTokenService.registerOrUpdateToken(fcmTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    fcmTokenRequest.getUserId(), DeviceType.ANDROID);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isEqualTo("test-fcm-token");
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("Galaxy S23");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.0.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("13");
            assertThat(savedToken.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("기존 FCM 토큰이 성공적으로 업데이트된다")
        void updateExistingToken_Success() {
            // given
            UserFCMToken existingToken = tokenRepository.save(UserFCMToken.builder()
                    .userId(1L)
                    .fcmToken("existing-fcm-token")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S22")
                    .appVersion("0.9.0")
                    .osVersion("12")
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now().minusDays(1))
                    .build());

            // when
            fcmTokenService.registerOrUpdateToken(fcmTokenRequest);

            // then
            UserFCMToken updatedToken = tokenRepository.findById(existingToken.getId()).orElseThrow();
            assertThat(updatedToken.getFcmToken()).isEqualTo("test-fcm-token");
            assertThat(updatedToken.getDeviceModel()).isEqualTo("Galaxy S23");
            assertThat(updatedToken.getAppVersion()).isEqualTo("1.0.0");
            assertThat(updatedToken.getOsVersion()).isEqualTo("13");
            assertThat(updatedToken.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("FCM 토큰이 null이어도 디바이스 정보가 저장된다")
        void registerDeviceInfoWithNullToken_Success() {
            // given
            FCMTokenRequest nullTokenRequest = FCMTokenRequest.builder()
                    .userId(2L)
                    .fcmToken(null)
                    .deviceType("ios")
                    .deviceModel("iPhone 14 Pro")
                    .appVersion("2.0.0")
                    .osVersion("16")
                    .build();

            // when
            fcmTokenService.registerOrUpdateToken(nullTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    2L, DeviceType.IOS);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isNull();
            assertThat(savedToken.get().getDeviceType()).isEqualTo(DeviceType.IOS);
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("iPhone 14 Pro");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("2.0.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("16");
            assertThat(savedToken.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("FCM 토큰이 빈 문자열이어도 디바이스 정보가 저장된다")
        void registerDeviceInfoWithEmptyToken_Success() {
            // given
            FCMTokenRequest emptyTokenRequest = FCMTokenRequest.builder()
                    .userId(3L)
                    .fcmToken(null)
                    .deviceType("android")
                    .deviceModel("Pixel 7")
                    .appVersion("1.5.0")
                    .osVersion("13")
                    .build();

            // when
            fcmTokenService.registerOrUpdateToken(emptyTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    3L, DeviceType.ANDROID);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isNull();
            assertThat(savedToken.get().getDeviceType()).isEqualTo(DeviceType.ANDROID);
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("Pixel 7");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.5.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("13");
        }

        @Test
        @DisplayName("잘못된 디바이스 타입으로 예외가 발생해도 처리된다")
        void handleInvalidDeviceType() {
            // given
            FCMTokenRequest invalidDeviceRequest = FCMTokenRequest.builder()
                    .userId(4L)
                    .fcmToken("test-token")
                    .deviceType("invalid-device")
                    .deviceModel("Test Device")
                    .appVersion("1.0.0")
                    .osVersion("1.0")
                    .build();

            // when & then
            assertThatCode(() -> fcmTokenService.registerOrUpdateToken(invalidDeviceRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("FCM 토큰 조회")
    class FindTokens {

        @Test
        @DisplayName("FCM 토큰이 있는 디바이스만 조회된다")
        void findActiveTokensByUserId_OnlyWithTokens() {
            // given
            Long userId = 20L;
            
            // FCM 토큰이 있는 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("valid-token-1")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S23")
                    .isActive(true)
                    .build());
                    
            // FCM 토큰이 null인 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken(null)
                    .deviceType(DeviceType.IOS)
                    .deviceModel("iPhone 14")
                    .isActive(true)
                    .build());
                    
            // FCM 토큰이 있는 다른 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("valid-token-2")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Pixel 8")
                    .isActive(true)
                    .build());

            // when
            List<UserFCMToken> tokensWithFcm = tokenRepository.findActiveTokensByUserId(userId);
            List<UserFCMToken> allDevices = tokenRepository.findActiveDevicesByUserId(userId);

            // then
            assertThat(tokensWithFcm).hasSize(2);
            assertThat(tokensWithFcm).allMatch(token -> token.getFcmToken() != null);
            assertThat(allDevices).hasSize(3);
        }

        @Test
        @DisplayName("여러 사용자의 FCM 토큰이 있는 디바이스만 조회된다")
        void findActiveTokensByUserIds_OnlyWithTokens() {
            // given
            Long userId1 = 21L;
            Long userId2 = 22L;
            
            // 사용자1 - FCM 토큰 있음
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId1)
                    .fcmToken("user1-token")
                    .deviceType(DeviceType.ANDROID)
                    .isActive(true)
                    .build());
                    
            // 사용자1 - FCM 토큰 null
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId1)
                    .fcmToken(null)
                    .deviceType(DeviceType.IOS)
                    .isActive(true)
                    .build());
                    
            // 사용자2 - FCM 토큰 있음
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId2)
                    .fcmToken("user2-token")
                    .deviceType(DeviceType.IOS)
                    .isActive(true)
                    .build());

            // when
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserIds(List.of(userId1, userId2));

            // then
            assertThat(tokens).hasSize(2);
            assertThat(tokens).allMatch(token -> token.getFcmToken() != null);
            assertThat(tokens).extracting(UserFCMToken::getUserId)
                    .containsExactlyInAnyOrder(userId1, userId2);
        }

        @Test
        @DisplayName("비활성화된 디바이스는 조회되지 않는다")
        void findActiveTokensByUserId_OnlyActiveDevices() {
            // given
            Long userId = 23L;
            
            // 활성화된 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("active-token")
                    .deviceType(DeviceType.ANDROID)
                    .isActive(true)
                    .build());
                    
            // 비활성화된 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("inactive-token")
                    .deviceType(DeviceType.IOS)
                    .isActive(false)
                    .build());

            // when
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserId(userId);

            // then
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo("active-token");
            assertThat(tokens.get(0).getIsActive()).isTrue();
        }
    }
}