package com.example.cherrydan.fcm.service;

import com.example.cherrydan.common.exception.FCMException;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenRequest;
import com.example.cherrydan.fcm.dto.FCMTokenResponseDTO;
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
                .isAllowed(true)
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
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceModelAndIsActiveTrue(
                    fcmTokenRequest.getUserId(), "Galaxy S23");
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isEqualTo("test-fcm-token");
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("Galaxy S23");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.0.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("13");
            assertThat(savedToken.get().getIsActive()).isTrue();
            assertThat(savedToken.get().getIsAllowed()).isTrue();
        }

        @Test
        @DisplayName("기존 FCM 토큰이 성공적으로 업데이트된다")
        void updateExistingToken_Success() {
            // given
            UserFCMToken existingToken = tokenRepository.save(UserFCMToken.builder()
                    .userId(1L)
                    .fcmToken("existing-fcm-token")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S23")
                    .appVersion("0.9.0")
                    .osVersion("12")
                    .isActive(true)
                    .isAllowed(true)
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
            assertThat(updatedToken.getIsAllowed()).isTrue();
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
                    .isAllowed(true)
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
        @DisplayName("활성화되고 알림이 허용된 디바이스만 조회된다")
        void findActiveTokensByUserId_OnlyAllowedTokens() {
            // given
            Long userId = 20L;
            
            // FCM 토큰이 있는 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("valid-token-1")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S23")
                    .isActive(true)
                    .isAllowed(true)
                    .build());
                    
                    
            // FCM 토큰이 있는 다른 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("valid-token-2")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Pixel 8")
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            // when
            List<UserFCMToken> tokensWithFcm = tokenRepository.findActiveTokensByUserId(userId);
            List<UserFCMToken> allDevices = tokenRepository.findActiveDevicesByUserId(userId);

            // then
            assertThat(tokensWithFcm).hasSize(2);
            assertThat(allDevices).hasSize(2);
        }

        @Test
        @DisplayName("여러 사용자의 활성화되고 알림이 허용된 디바이스가 조회된다")
        void findActiveTokensByUserIds_OnlyAllowedTokens() {
            // given
            Long userId1 = 21L;
            Long userId2 = 22L;
            
            // 사용자1 - FCM 토큰 있음
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId1)
                    .fcmToken("user1-token")
                    .deviceType(DeviceType.ANDROID)
                    .isActive(true)
                    .isAllowed(true)
                    .build());
                    
                    
            // 사용자2 - FCM 토큰 있음
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId2)
                    .fcmToken("user2-token")
                    .deviceType(DeviceType.IOS)
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            // when
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserIds(List.of(userId1, userId2));

            // then
            assertThat(tokens).hasSize(2);
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
                    .isAllowed(true)
                    .build());
                    
            // 비활성화된 디바이스
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("inactive-token")
                    .deviceType(DeviceType.IOS)
                    .isActive(false)
                    .isAllowed(true)
                    .build());

            // when
            List<UserFCMToken> tokens = tokenRepository.findActiveTokensByUserId(userId);

            // then
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo("active-token");
            assertThat(tokens.get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("FCM 토큰 수정")
    class UpdateFCMToken {

        @Test
        @DisplayName("특정 디바이스의 FCM 토큰이 성공적으로 수정된다")
        void updateFCMToken_Success() {
            // given
            Long userId = 100L;
            UserFCMToken savedToken = tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("old-token")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S23")
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            String newToken = "new-updated-token";

            // when
            fcmTokenService.updateFCMToken(userId, savedToken.getId(), newToken);

            // then
            UserFCMToken updatedToken = tokenRepository.findById(savedToken.getId()).orElseThrow();
            assertThat(updatedToken.getFcmToken()).isEqualTo(newToken);
            assertThat(updatedToken.getDeviceType()).isEqualTo(DeviceType.ANDROID);
            assertThat(updatedToken.getDeviceModel()).isEqualTo("Galaxy S23");
        }

        @Test
        @DisplayName("존재하지 않는 디바이스 ID로 FCM 토큰 수정 시 예외가 발생한다")
        void updateFCMToken_DeviceNotFound() {
            // given
            Long userId = 101L;
            Long nonExistentDeviceId = 999L;
            String newToken = "new-token";

            // when & then
            assertThatThrownBy(() -> fcmTokenService.updateFCMToken(userId, nonExistentDeviceId, newToken))
                    .isInstanceOf(FCMException.class);
        }

        @Test
        @DisplayName("다른 사용자의 디바이스 FCM 토큰 수정 시 접근 거부 예외가 발생한다")
        void updateFCMToken_AccessDenied() {
            // given
            Long ownerUserId = 102L;
            Long unauthorizedUserId = 103L;
            
            UserFCMToken savedToken = tokenRepository.save(UserFCMToken.builder()
                    .userId(ownerUserId)
                    .fcmToken("original-token")
                    .deviceType(DeviceType.ANDROID)
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            String newToken = "unauthorized-token";

            // when & then
            assertThatThrownBy(() -> fcmTokenService.updateFCMToken(unauthorizedUserId, savedToken.getId(), newToken))
                    .isInstanceOf(FCMException.class);
        }

        @Test
        @DisplayName("null 또는 빈 문자열 FCM 토큰으로 수정 시 토큰이 변경되지 않는다")
        void updateFCMToken_NullOrEmptyToken() {
            // given
            Long userId = 104L;
            String originalToken = "original-token";
            UserFCMToken savedToken = tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken(originalToken)
                    .deviceType(DeviceType.IOS)
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            // when - null 토큰으로 수정 시도
            fcmTokenService.updateFCMToken(userId, savedToken.getId(), null);

            // then
            UserFCMToken tokenAfterNull = tokenRepository.findById(savedToken.getId()).orElseThrow();
            assertThat(tokenAfterNull.getFcmToken()).isEqualTo(originalToken);

            // when - 빈 문자열 토큰으로 수정 시도
            fcmTokenService.updateFCMToken(userId, savedToken.getId(), "");

            // then
            UserFCMToken tokenAfterEmpty = tokenRepository.findById(savedToken.getId()).orElseThrow();
            assertThat(tokenAfterEmpty.getFcmToken()).isEqualTo(originalToken);

            // when - 공백만 있는 토큰으로 수정 시도
            fcmTokenService.updateFCMToken(userId, savedToken.getId(), "   ");

            // then
            UserFCMToken tokenAfterWhitespace = tokenRepository.findById(savedToken.getId()).orElseThrow();
            assertThat(tokenAfterWhitespace.getFcmToken()).isEqualTo(originalToken);
        }
    }

    @Nested
    @DisplayName("사용자 FCM 토큰 조회")
    class GetUserFCMTokens {

        @Test
        @DisplayName("사용자의 활성화된 FCM 토큰들이 성공적으로 조회된다")
        void getUserFCMTokens_Success() {
            // given
            Long userId = 200L;
            
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("token-1")
                    .deviceType(DeviceType.ANDROID)
                    .deviceModel("Galaxy S23")
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("token-2")
                    .deviceType(DeviceType.IOS)
                    .deviceModel("iPhone 14")
                    .isActive(true)
                    .isAllowed(true)
                    .build());

            // 비활성화된 토큰 (조회되지 않아야 함)
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("inactive-token")
                    .deviceType(DeviceType.ANDROID)
                    .isActive(false)
                    .isAllowed(true)
                    .build());

            // 알림 비허용 디바이스 (조회되지 않아야 함)
            tokenRepository.save(UserFCMToken.builder()
                    .userId(userId)
                    .fcmToken("notification-disabled-token")
                    .deviceType(DeviceType.IOS)
                    .deviceModel("iPhone 15")
                    .isActive(true)
                    .isAllowed(false)
                    .build());

            // when
            List<FCMTokenResponseDTO> userTokens = fcmTokenService.getUserFCMTokens(userId);

            // then
            assertThat(userTokens).hasSize(2);
            assertThat(userTokens).extracting(FCMTokenResponseDTO::getFcmToken)
                    .containsExactlyInAnyOrder("token-1", "token-2");
            assertThat(userTokens).allMatch(FCMTokenResponseDTO::getIsActive);
            assertThat(userTokens).allMatch(FCMTokenResponseDTO::getIsAllowed);
        }

        @Test
        @DisplayName("FCM 토큰이 없는 사용자는 빈 리스트가 반환된다")
        void getUserFCMTokens_EmptyList() {
            // given
            Long userIdWithNoTokens = 201L;

            // when
            List<FCMTokenResponseDTO> userTokens = fcmTokenService.getUserFCMTokens(userIdWithNoTokens);

            // then
            assertThat(userTokens).isEmpty();
        }
    }
}