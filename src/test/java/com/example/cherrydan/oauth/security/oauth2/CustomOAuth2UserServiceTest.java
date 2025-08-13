package com.example.cherrydan.oauth.security.oauth2;

import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.oauth.dto.AppleLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("CustomOAuth2UserService FCM 관련 통합테스트")
class CustomOAuth2UserServiceTest {

    @Autowired
    private UserFCMTokenRepository tokenRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    private AppleLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new AppleLoginRequest();
        loginRequest.setFcmToken("test-fcm-token");
        loginRequest.setDeviceType("android");
        loginRequest.setDeviceModel("Galaxy S23");
        loginRequest.setAppVersion("1.0.0");
        loginRequest.setOsVersion("13");
    }

    @Nested
    @DisplayName("FCM 토큰 등록 테스트")
    class RegisterFCMTokenIfPresent {

        @Test
        @DisplayName("FCM 토큰이 있을 때 성공적으로 등록된다")
        void registerFCMToken_WithValidToken_Success() {
            // given
            Long userId = 1L;

            // when
            customOAuth2UserService.registerFCMTokenIfPresent(userId, loginRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    userId, DeviceType.ANDROID);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isEqualTo("test-fcm-token");
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("Galaxy S23");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.0.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("13");
        }

        @Test
        @DisplayName("FCM 토큰이 null일 때도 디바이스 정보는 저장된다")
        void registerDeviceInfo_WithNullToken_Success() {
            // given
            Long userId = 2L;
            AppleLoginRequest nullTokenRequest = new AppleLoginRequest();
            nullTokenRequest.setFcmToken(null);
            nullTokenRequest.setDeviceType("ios");
            nullTokenRequest.setDeviceModel("iPhone 14 Pro");
            nullTokenRequest.setAppVersion("2.0.0");
            nullTokenRequest.setOsVersion("16");

            // when
            customOAuth2UserService.registerFCMTokenIfPresent(userId, nullTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    userId, DeviceType.IOS);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isNull();
            assertThat(savedToken.get().getDeviceType()).isEqualTo(DeviceType.IOS);
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("iPhone 14 Pro");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("2.0.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("16");
        }

        @Test
        @DisplayName("FCM 토큰이 빈 문자열일 때도 디바이스 정보는 저장된다")
        void registerDeviceInfo_WithEmptyToken_Success() {
            // given
            Long userId = 3L;
            AppleLoginRequest emptyTokenRequest = new AppleLoginRequest();
            emptyTokenRequest.setFcmToken("");
            emptyTokenRequest.setDeviceType("android");
            emptyTokenRequest.setDeviceModel("Pixel 7");
            emptyTokenRequest.setAppVersion("1.5.0");
            emptyTokenRequest.setOsVersion("13");

            // when
            customOAuth2UserService.registerFCMTokenIfPresent(userId, emptyTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    userId, DeviceType.ANDROID);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isNull();
            assertThat(savedToken.get().getDeviceType()).isEqualTo(DeviceType.ANDROID);
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("Pixel 7");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.5.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("13");
        }

        @Test
        @DisplayName("FCM 토큰이 공백만 있을 때도 디바이스 정보는 저장된다")
        void registerDeviceInfo_WithWhitespaceToken_Success() {
            // given
            Long userId = 4L;
            AppleLoginRequest whitespaceTokenRequest = new AppleLoginRequest();
            whitespaceTokenRequest.setFcmToken("   ");
            whitespaceTokenRequest.setDeviceType("android");
            whitespaceTokenRequest.setDeviceModel("OnePlus 10");
            whitespaceTokenRequest.setAppVersion("1.2.0");
            whitespaceTokenRequest.setOsVersion("12");

            // when
            customOAuth2UserService.registerFCMTokenIfPresent(userId, whitespaceTokenRequest);

            // then
            Optional<UserFCMToken> savedToken = tokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(
                    userId, DeviceType.ANDROID);
            
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getFcmToken()).isNull();
            assertThat(savedToken.get().getDeviceModel()).isEqualTo("OnePlus 10");
            assertThat(savedToken.get().getAppVersion()).isEqualTo("1.2.0");
            assertThat(savedToken.get().getOsVersion()).isEqualTo("12");
        }
    }

}