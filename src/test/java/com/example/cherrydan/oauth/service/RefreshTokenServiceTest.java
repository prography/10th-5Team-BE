package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.RefreshTokenException;
import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.oauth.model.AuthProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;
    private final String VALID_REFRESH_TOKEN = "valid.refresh.token";
    private final String INVALID_TOKEN = "invalid.token";
    private final String ACCESS_TOKEN = "access.token";
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        testUser = User.builder()
                .id(USER_ID)
                .name("테스트 사용자")
                .nickname("testuser")
                .email("test@example.com")
                .birthYear(1990)
                .gender(Gender.MALE)
                .socialId("social123")
                .provider(AuthProvider.GOOGLE)
                .role(Role.ROLE_USER)
                .picture("profile.jpg")
                .isActive(true)
                .build();

        // 테스트용 RefreshToken 객체 생성
        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .refreshToken(VALID_REFRESH_TOKEN)
                .user(testUser)
                .build();
    }

    @Nested
    @DisplayName("saveOrUpdateRefreshToken 메서드 테스트")
    class SaveOrUpdateRefreshTokenTest {

        @Test
        @DisplayName("새로운 리프레시 토큰 저장 - 성공")
        void saveNewRefreshToken_Success() {
            // Given
            String newTokenValue = "new.refresh.token";
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, newTokenValue))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When
            refreshTokenService.saveOrUpdateRefreshToken(testUser, newTokenValue);

            // Then
            verify(refreshTokenRepository).findByUserIdAndRefreshToken(USER_ID, newTokenValue);
            verify(refreshTokenRepository).save(argThat(token -> 
                token.getRefreshToken().equals(newTokenValue) && 
                token.getUser().equals(testUser)
            ));
        }

        @Test
        @DisplayName("기존 리프레시 토큰 업데이트 - 성공")
        void updateExistingRefreshToken_Success() {
            // Given
            String updatedTokenValue = "updated.refresh.token";
            RefreshToken existingToken = RefreshToken.builder()
                    .id(1L)
                    .refreshToken("old.token")
                    .user(testUser)
                    .build();
            
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, updatedTokenValue))
                    .willReturn(Optional.of(existingToken));
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(existingToken);

            // When
            refreshTokenService.saveOrUpdateRefreshToken(testUser, updatedTokenValue);

            // Then
            verify(refreshTokenRepository).findByUserIdAndRefreshToken(USER_ID, updatedTokenValue);
            assertThat(existingToken.getRefreshToken()).isEqualTo(updatedTokenValue);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("null 사용자로 토큰 저장 시도 - 실패")
        void saveRefreshTokenWithNullUser_Failure() {
            // Given
            String tokenValue = "some.token";

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.saveOrUpdateRefreshToken(null, tokenValue))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 토큰 값으로 저장 시도 - 처리됨")
        void saveRefreshTokenWithNullValue_Handled() {
            // Given
            given(refreshTokenRepository.findByUserIdAndRefreshToken(eq(USER_ID), isNull()))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When
            refreshTokenService.saveOrUpdateRefreshToken(testUser, null);

            // Then
            verify(refreshTokenRepository).save(argThat(token -> 
                token.getRefreshToken() == null && 
                token.getUser().equals(testUser)
            ));
        }
    }

    @Nested
    @DisplayName("getRefreshTokenEntity 메서드 테스트")
    class GetRefreshTokenEntityTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 엔티티 조회 - 성공")
        void getValidRefreshTokenEntity_Success() {
            // Given
            given(jwtTokenProvider.isRefreshToken(VALID_REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(VALID_REFRESH_TOKEN)).willReturn(USER_ID);
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, VALID_REFRESH_TOKEN))
                    .willReturn(Optional.of(testRefreshToken));

            // When
            RefreshToken result = refreshTokenService.getRefreshTokenEntity(VALID_REFRESH_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRefreshToken()).isEqualTo(VALID_REFRESH_TOKEN);
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getUser().getIsActive()).isTrue();

            verify(jwtTokenProvider).validateToken(VALID_REFRESH_TOKEN);
            verify(jwtTokenProvider).isRefreshToken(VALID_REFRESH_TOKEN);
            verify(jwtTokenProvider).getUserIdFromToken(VALID_REFRESH_TOKEN);
            verify(refreshTokenRepository).findByUserIdAndRefreshToken(USER_ID, VALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("만료된 JWT 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithExpiredToken_Failure() {
            // Given
            String expiredToken = "expired.jwt.token";
            willThrow(new ExpiredJwtException(null, null, "Token expired"))
                    .given(jwtTokenProvider).validateToken(expiredToken);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class)
                    .hasMessageContaining("Token expired");

            verify(jwtTokenProvider).validateToken(expiredToken);
            verify(jwtTokenProvider, never()).isRefreshToken(any());
            verify(refreshTokenRepository, never()).findByUserIdAndRefreshToken(any(), any());
        }

        @Test
        @DisplayName("잘못된 형식의 JWT 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithMalformedToken_Failure() {
            // Given
            String malformedToken = "malformed.token";
            willThrow(new MalformedJwtException("Invalid token format"))
                    .given(jwtTokenProvider).validateToken(malformedToken);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(malformedToken))
                    .isInstanceOf(MalformedJwtException.class)
                    .hasMessageContaining("Invalid token format");

            verify(jwtTokenProvider).validateToken(malformedToken);
            verify(jwtTokenProvider, never()).isRefreshToken(any());
        }

        @Test
        @DisplayName("서명이 잘못된 JWT 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithInvalidSignature_Failure() {
            // Given
            String invalidSignatureToken = "invalid.signature.token";
            willThrow(new SignatureException("Invalid signature"))
                    .given(jwtTokenProvider).validateToken(invalidSignatureToken);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(invalidSignatureToken))
                    .isInstanceOf(SignatureException.class)
                    .hasMessageContaining("Invalid signature");

            verify(jwtTokenProvider).validateToken(invalidSignatureToken);
        }

        @Test
        @DisplayName("Access Token으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithAccessToken_Failure() {
            // Given
            given(jwtTokenProvider.isRefreshToken(ACCESS_TOKEN)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(ACCESS_TOKEN))
                    .isInstanceOf(RefreshTokenException.class)
                    .extracting("errorMessage")
                    .isEqualTo(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);

            verify(jwtTokenProvider).validateToken(ACCESS_TOKEN);
            verify(jwtTokenProvider).isRefreshToken(ACCESS_TOKEN);
            verify(jwtTokenProvider, never()).getUserIdFromToken(any());
            verify(refreshTokenRepository, never()).findByUserIdAndRefreshToken(any(), any());
        }

        @Test
        @DisplayName("DB에 존재하지 않는 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithNonExistentToken_Failure() {
            // Given
            String nonExistentToken = "non.existent.token";
            given(jwtTokenProvider.isRefreshToken(nonExistentToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(nonExistentToken)).willReturn(USER_ID);
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, nonExistentToken))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(nonExistentToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .extracting("errorMessage")
                    .isEqualTo(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);

            verify(jwtTokenProvider).validateToken(nonExistentToken);
            verify(jwtTokenProvider).isRefreshToken(nonExistentToken);
            verify(jwtTokenProvider).getUserIdFromToken(nonExistentToken);
            verify(refreshTokenRepository).findByUserIdAndRefreshToken(USER_ID, nonExistentToken);
        }

        @Test
        @DisplayName("비활성 사용자의 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithInactiveUser_Failure() {
            // Given
            User inactiveUser = User.builder()
                    .id(USER_ID)
                    .name("비활성 사용자")
                    .email("inactive@example.com")
                    .isActive(false) // 비활성 상태
                    .build();

            RefreshToken inactiveUserToken = RefreshToken.builder()
                    .id(1L)
                    .refreshToken(VALID_REFRESH_TOKEN)
                    .user(inactiveUser)
                    .build();

            given(jwtTokenProvider.isRefreshToken(VALID_REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(VALID_REFRESH_TOKEN)).willReturn(USER_ID);
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, VALID_REFRESH_TOKEN))
                    .willReturn(Optional.of(inactiveUserToken));

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(VALID_REFRESH_TOKEN))
                    .isInstanceOf(RefreshTokenException.class)
                    .extracting("errorMessage")
                    .isEqualTo(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);

            verify(jwtTokenProvider).validateToken(VALID_REFRESH_TOKEN);
            verify(jwtTokenProvider).isRefreshToken(VALID_REFRESH_TOKEN);
            verify(jwtTokenProvider).getUserIdFromToken(VALID_REFRESH_TOKEN);
            verify(refreshTokenRepository).findByUserIdAndRefreshToken(USER_ID, VALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("null 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithNullToken_Failure() {
            // Given
            willThrow(new IllegalArgumentException("Token cannot be null"))
                    .given(jwtTokenProvider).validateToken(null);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token cannot be null");

            verify(jwtTokenProvider).validateToken(null);
        }

        @Test
        @DisplayName("빈 문자열 토큰으로 조회 시도 - 실패")
        void getRefreshTokenEntityWithEmptyToken_Failure() {
            // Given
            String emptyToken = "";
            willThrow(new IllegalArgumentException("Token cannot be empty"))
                    .given(jwtTokenProvider).validateToken(emptyToken);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(emptyToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token cannot be empty");

            verify(jwtTokenProvider).validateToken(emptyToken);
        }
    }

    @Nested
    @DisplayName("deleteRefreshTokenByUserId 메서드 테스트")
    class DeleteRefreshTokenByUserIdTest {

        @Test
        @DisplayName("유효한 사용자 ID로 토큰 삭제 - 성공")
        void deleteRefreshTokenByValidUserId_Success() {
            // Given
            willDoNothing().given(refreshTokenRepository).deleteByUserId(USER_ID);

            // When
            refreshTokenService.deleteRefreshTokenByUserId(USER_ID);

            // Then
            verify(refreshTokenRepository).deleteByUserId(USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 토큰 삭제 시도 - 성공 (아무것도 삭제되지 않음)")
        void deleteRefreshTokenByNonExistentUserId_Success() {
            // Given
            Long nonExistentUserId = 999L;
            willDoNothing().given(refreshTokenRepository).deleteByUserId(nonExistentUserId);

            // When
            refreshTokenService.deleteRefreshTokenByUserId(nonExistentUserId);

            // Then
            verify(refreshTokenRepository).deleteByUserId(nonExistentUserId);
        }

        @Test
        @DisplayName("데이터베이스 오류로 인한 토큰 삭제 실패")
        void deleteRefreshTokenWithDatabaseError_Failure() {
            // Given
            RuntimeException databaseException = new RuntimeException("Database connection failed");
            willThrow(databaseException).given(refreshTokenRepository).deleteByUserId(USER_ID);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.deleteRefreshTokenByUserId(USER_ID))
                    .isInstanceOf(RefreshTokenException.class)
                    .extracting("errorMessage")
                    .isEqualTo(ErrorMessage.REFRESH_TOKEN_DELETE_ERROR);

            verify(refreshTokenRepository).deleteByUserId(USER_ID);
        }

        @Test
        @DisplayName("null 사용자 ID로 토큰 삭제 시도 - 실패")
        void deleteRefreshTokenByNullUserId_Failure() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("User ID cannot be null");
            willThrow(exception).given(refreshTokenRepository).deleteByUserId(null);

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.deleteRefreshTokenByUserId(null))
                    .isInstanceOf(RefreshTokenException.class)
                    .extracting("errorMessage")
                    .isEqualTo(ErrorMessage.REFRESH_TOKEN_DELETE_ERROR);

            verify(refreshTokenRepository).deleteByUserId(null);
        }

        @Test
        @DisplayName("음수 사용자 ID로 토큰 삭제 시도 - 처리됨")
        void deleteRefreshTokenByNegativeUserId_Handled() {
            // Given
            Long negativeUserId = -1L;
            willDoNothing().given(refreshTokenRepository).deleteByUserId(negativeUserId);

            // When
            refreshTokenService.deleteRefreshTokenByUserId(negativeUserId);

            // Then
            verify(refreshTokenRepository).deleteByUserId(negativeUserId);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("전체 토큰 라이프사이클 테스트 - 생성, 조회, 삭제")
        void completeTokenLifecycle_Success() {
            // Given
            String tokenValue = "lifecycle.test.token";

            // 1. 토큰 저장
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, tokenValue))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // 2. 토큰 조회를 위한 Mock 설정
            given(jwtTokenProvider.isRefreshToken(tokenValue)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(tokenValue)).willReturn(USER_ID);
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, tokenValue))
                    .willReturn(Optional.of(testRefreshToken));

            // 3. 토큰 삭제를 위한 Mock 설정
            willDoNothing().given(refreshTokenRepository).deleteByUserId(USER_ID);

            // When & Then
            // 1. 토큰 저장
            assertThatCode(() -> refreshTokenService.saveOrUpdateRefreshToken(testUser, tokenValue))
                    .doesNotThrowAnyException();

            // 2. 토큰 조회
            RefreshToken retrievedToken = refreshTokenService.getRefreshTokenEntity(tokenValue);
            assertThat(retrievedToken).isNotNull();
            assertThat(retrievedToken.getUser().getIsActive()).isTrue();

            // 3. 토큰 삭제
            assertThatCode(() -> refreshTokenService.deleteRefreshTokenByUserId(USER_ID))
                    .doesNotThrowAnyException();

            // 검증
            verify(refreshTokenRepository).save(any(RefreshToken.class));
            verify(jwtTokenProvider).validateToken(tokenValue);
            verify(refreshTokenRepository).deleteByUserId(USER_ID);
        }

        @Test
        @DisplayName("동일한 사용자의 다중 토큰 처리 테스트")
        void multipleTokensForSameUser_Handled() {
            // Given
            String firstToken = "first.token";
            String secondToken = "second.token";

            given(refreshTokenRepository.findByUserIdAndRefreshToken(eq(USER_ID), anyString()))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When
            refreshTokenService.saveOrUpdateRefreshToken(testUser, firstToken);
            refreshTokenService.saveOrUpdateRefreshToken(testUser, secondToken);

            // Then
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("에지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("매우 긴 토큰 값 처리")
        void handleVeryLongTokenValue() {
            // Given
            String longToken = "a".repeat(1000); // 1000자 토큰
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, longToken))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When & Then
            assertThatCode(() -> refreshTokenService.saveOrUpdateRefreshToken(testUser, longToken))
                    .doesNotThrowAnyException();

            verify(refreshTokenRepository).save(argThat(token -> 
                token.getRefreshToken().equals(longToken)
            ));
        }

        @Test
        @DisplayName("특수 문자가 포함된 토큰 값 처리")
        void handleTokenWithSpecialCharacters() {
            // Given
            String specialToken = "token.with-special_chars!@#$%^&*()";
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, specialToken))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When & Then
            assertThatCode(() -> refreshTokenService.saveOrUpdateRefreshToken(testUser, specialToken))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유니코드 문자가 포함된 토큰 값 처리")
        void handleTokenWithUnicodeCharacters() {
            // Given
            String unicodeToken = "토큰.with.한글.and.emoji.😀";
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, unicodeToken))
                    .willReturn(Optional.empty());
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When & Then
            assertThatCode(() -> refreshTokenService.saveOrUpdateRefreshToken(testUser, unicodeToken))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("동시성 시나리오 시뮬레이션 - 같은 시간에 토큰 저장")
        void concurrentTokenSave_Simulation() {
            // Given
            String concurrentToken = "concurrent.token";
            
            // 첫 번째 호출에서는 토큰이 없음
            given(refreshTokenRepository.findByUserIdAndRefreshToken(USER_ID, concurrentToken))
                    .willReturn(Optional.empty())
                    .willReturn(Optional.of(testRefreshToken)); // 두 번째 호출에서는 토큰 존재
            
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willReturn(testRefreshToken);

            // When
            refreshTokenService.saveOrUpdateRefreshToken(testUser, concurrentToken);
            refreshTokenService.saveOrUpdateRefreshToken(testUser, concurrentToken);

            // Then
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }
    }
}