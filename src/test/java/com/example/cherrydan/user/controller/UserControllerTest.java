package com.example.cherrydan.user.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.EmptyResponse;
import com.example.cherrydan.fcm.domain.DeviceType;
import com.example.cherrydan.fcm.domain.UserFCMToken;
import com.example.cherrydan.fcm.dto.FCMTokenUpdateRequest;
import com.example.cherrydan.fcm.dto.FCMTokenResponseDTO;
import com.example.cherrydan.fcm.service.FCMTokenService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController FCM 관련 테스트")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private FCMTokenService fcmTokenService;

    @Mock
    private UserDetailsImpl currentUser;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        given(currentUser.getId()).willReturn(1L);
    }

    @Test
    @DisplayName("사용자 FCM 토큰 조회 성공")
    void getUserFCMTokens_Success() {
        // given
        UserFCMToken token1 = UserFCMToken.builder()
                .id(1L)
                .userId(1L)
                .fcmToken("token1")
                .deviceType(DeviceType.ANDROID)
                .deviceModel("Galaxy S23")
                .isActive(true)
                .build();

        UserFCMToken token2 = UserFCMToken.builder()
                .id(2L)
                .userId(1L)
                .fcmToken("token2")
                .deviceType(DeviceType.IOS)
                .deviceModel("iPhone 14")
                .isActive(true)
                .build();

        List<FCMTokenResponseDTO> tokenDTOs = Arrays.asList(
                FCMTokenResponseDTO.from(token1),
                FCMTokenResponseDTO.from(token2)
        );
        given(fcmTokenService.getUserFCMTokens(1L)).willReturn(tokenDTOs);

        // when
        ResponseEntity<ApiResponse<List<FCMTokenResponseDTO>>> response = userController.getUserFCMTokens(currentUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("FCM 토큰 조회가 완료되었습니다.");
        assertThat(response.getBody().getResult()).hasSize(2);
        assertThat(response.getBody().getResult().get(0).getDeviceId()).isEqualTo(1L);
        assertThat(response.getBody().getResult().get(1).getDeviceId()).isEqualTo(2L);
        
        verify(fcmTokenService).getUserFCMTokens(1L);
    }

    @Test
    @DisplayName("FCM 토큰 수정 성공")
    void updateFCMToken_Success() {
        // given
        FCMTokenUpdateRequest request = FCMTokenUpdateRequest.builder()
                .deviceId(1L)
                .fcmToken("new-fcm-token")
                .build();

        doNothing().when(fcmTokenService).updateFCMToken(eq(1L), eq(1L), eq("new-fcm-token"));

        // when
        ResponseEntity<ApiResponse<EmptyResponse>> response = userController.updateFCMToken(currentUser, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("FCM 토큰이 수정되었습니다.");
        
        verify(fcmTokenService).updateFCMToken(1L, 1L, "new-fcm-token");
    }
}