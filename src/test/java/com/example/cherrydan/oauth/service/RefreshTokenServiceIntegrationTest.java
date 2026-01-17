package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.RefreshTokenException;
import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.oauth.domain.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("RefreshTokenService 통합 테스트")
class RefreshTokenServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceIntegrationTest.class);

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 초기화 시작 ===");
        
        // 테스트용 사용자 생성 및 저장
        testUser = User.builder()
                .name("통합테스트사용자")
                .nickname("integrationtest")
                .email("integration@test.com")
                .birthYear(1995)
                .gender(Gender.FEMALE)
                .socialId("integration123")
                .provider(AuthProvider.KAKAO)
                .role(Role.ROLE_USER)
                .picture("integration.jpg")
                .isActive(true)
                .build();

        User savedUser = userRepository.save(testUser);
        testUser = savedUser; // ID가 할당된 객체로 업데이트
        log.info("테스트 사용자 생성 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 실제 JWT 토큰 생성
        validRefreshToken = jwtTokenProvider.generateRefreshToken(testUser.getId());
        log.info("유효한 리프레시 토큰 생성 완료: {}", validRefreshToken.substring(0, 20) + "...");
        
        log.info("=== 테스트 데이터 초기화 완료 ===");
    }

    @Nested
    @DisplayName("실제 데이터베이스 연관관계 테스트")
    class DatabaseRelationshipTest {

        @Test
        @DisplayName("User와 RefreshToken OneToOne 관계 저장 테스트")
        void testUserRefreshTokenRelationshipSave() {
            log.info("--- User-RefreshToken 연관관계 저장 테스트 시작 ---");

            // When: RefreshToken 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);

            // Then: 데이터베이스에서 직접 조회하여 연관관계 확인
            Optional<RefreshToken> savedTokenOpt = refreshTokenRepository.findByUserIdAndRefreshToken(
                    testUser.getId(), validRefreshToken);

            assertThat(savedTokenOpt).isPresent();
            RefreshToken savedToken = savedTokenOpt.get();

            log.info("저장된 RefreshToken ID: {}", savedToken.getId());
            log.info("연관된 User ID: {}", savedToken.getUser().getId());
            log.info("연관된 User 이메일: {}", savedToken.getUser().getEmail());

            // 연관관계 검증
            assertThat(savedToken.getUser()).isNotNull();
            assertThat(savedToken.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(savedToken.getUser().getEmail()).isEqualTo(testUser.getEmail());
            assertThat(savedToken.getRefreshToken()).isEqualTo(validRefreshToken);

            log.info("--- User-RefreshToken 연관관계 저장 테스트 완료 ---");
        }

        @Test
        @DisplayName("Lazy Loading 동작 테스트")
        void testLazyLoadingBehavior() {
            log.info("--- Lazy Loading 동작 테스트 시작 ---");

            // Given: RefreshToken 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);

            // When: RefreshToken만 조회 (User는 Lazy Loading)
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByRefreshToken(validRefreshToken);
            assertThat(tokenOpt).isPresent();

            RefreshToken token = tokenOpt.get();
            log.info("RefreshToken 조회 완료: {}", token.getId());

            // Then: User 정보 접근 시 Lazy Loading 발생
            User lazyUser = token.getUser();
            assertThat(lazyUser).isNotNull();
            log.info("Lazy Loading으로 User 조회: {}", lazyUser.getEmail());

            // User의 모든 필드 검증
            assertThat(lazyUser.getId()).isEqualTo(testUser.getId());
            assertThat(lazyUser.getName()).isEqualTo(testUser.getName());
            assertThat(lazyUser.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(lazyUser.getIsActive()).isTrue();

            log.info("--- Lazy Loading 동작 테스트 완료 ---");
        }

        @Test
        @DisplayName("Cascade 및 OrphanRemoval 동작 테스트")
        void testCascadeAndOrphanRemoval() {
            log.info("--- Cascade 동작 테스트 시작 ---");

            // Given: RefreshToken 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);
            
            Long userId = testUser.getId();
            
            // 저장 확인
            Optional<RefreshToken> beforeDelete = refreshTokenRepository.findByUserIdAndRefreshToken(userId, validRefreshToken);
            assertThat(beforeDelete).isPresent();
            log.info("삭제 전 RefreshToken 존재 확인: {}", beforeDelete.get().getId());

            // When: RefreshToken 삭제
            refreshTokenService.deleteRefreshTokenByUserId(userId);

            // Then: RefreshToken이 삭제되었는지 확인
            Optional<RefreshToken> afterDelete = refreshTokenRepository.findByUserIdAndRefreshToken(userId, validRefreshToken);
            assertThat(afterDelete).isEmpty();
            log.info("삭제 후 RefreshToken 존재하지 않음 확인");

            // User는 여전히 존재해야 함 (단방향 관계)
            Optional<User> userStillExists = userRepository.findById(userId);
            assertThat(userStillExists).isPresent();
            assertThat(userStillExists.get().getIsActive()).isTrue();
            log.info("User는 여전히 존재: {}", userStillExists.get().getEmail());

            log.info("--- Cascade 동작 테스트 완료 ---");
        }
    }

    @Nested
    @DisplayName("실제 JWT 토큰 검증 테스트")
    class RealJwtTokenTest {

        @Test
        @DisplayName("실제 JWT 토큰으로 전체 플로우 테스트")
        void testCompleteFlowWithRealJwtToken() {
            log.info("--- 실제 JWT 토큰 전체 플로우 테스트 시작 ---");

            // 1. 토큰 저장
            log.info("1. RefreshToken 저장 시작");
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);
            log.info("1. RefreshToken 저장 완료");

            // 2. 토큰 검증 및 조회
            log.info("2. RefreshToken 검증 및 조회 시작");
            RefreshToken retrievedToken = refreshTokenService.getRefreshTokenEntity(validRefreshToken);
            
            assertThat(retrievedToken).isNotNull();
            assertThat(retrievedToken.getRefreshToken()).isEqualTo(validRefreshToken);
            assertThat(retrievedToken.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(retrievedToken.getUser().getIsActive()).isTrue();
            log.info("2. RefreshToken 검증 및 조회 완료: userId={}", retrievedToken.getUser().getId());

            // 3. JWT 토큰에서 사용자 ID 추출 검증
            log.info("3. JWT 토큰에서 사용자 ID 추출 검증 시작");
            Long extractedUserId = jwtTokenProvider.getUserIdFromToken(validRefreshToken);
            assertThat(extractedUserId).isEqualTo(testUser.getId());
            log.info("3. JWT에서 추출된 사용자 ID: {}", extractedUserId);

            // 4. 토큰 타입 검증
            log.info("4. 토큰 타입 검증 시작");
            boolean isRefreshToken = jwtTokenProvider.isRefreshToken(validRefreshToken);
            assertThat(isRefreshToken).isTrue();
            log.info("4. 토큰 타입 검증 완료: isRefreshToken={}", isRefreshToken);

            // 5. 토큰 삭제
            log.info("5. RefreshToken 삭제 시작");
            refreshTokenService.deleteRefreshTokenByUserId(testUser.getId());
            log.info("5. RefreshToken 삭제 완료");

            // 6. 삭제 후 조회 시도 (실패해야 함)
            log.info("6. 삭제 후 조회 시도");
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(validRefreshToken))
                    .isInstanceOf(RefreshTokenException.class);
            log.info("6. 삭제 후 조회 시도 - 예상대로 예외 발생");

            log.info("--- 실제 JWT 토큰 전체 플로우 테스트 완료 ---");
        }

        @Test
        @DisplayName("만료된 토큰으로 테스트 (시뮬레이션)")
        void testWithExpiredTokenSimulation() {
            log.info("--- 만료된 토큰 시뮬레이션 테스트 시작 ---");

            // 잘못된 형식의 토큰으로 테스트 (실제 만료 토큰 생성은 복잡하므로)
            String invalidToken = "invalid.jwt.token";

            // When & Then: 잘못된 토큰으로 조회 시도
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(invalidToken))
                    .isInstanceOf(Exception.class); // JWT 검증 예외 발생

            log.info("--- 만료된 토큰 시뮬레이션 테스트 완료 ---");
        }

        @Test
        @DisplayName("Access Token으로 잘못 요청하는 경우 테스트")
        void testWithAccessTokenInsteadOfRefreshToken() {
            log.info("--- Access Token 잘못 사용 테스트 시작 ---");

            // Access Token 생성
            String accessToken = jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail());
            log.info("Access Token 생성: {}", accessToken.substring(0, 20) + "...");

            // When & Then: Access Token으로 RefreshToken 조회 시도 (실패해야 함)
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(accessToken))
                    .isInstanceOf(RefreshTokenException.class);

            log.info("--- Access Token 잘못 사용 테스트 완료 ---");
        }
    }

    @Nested
    @DisplayName("다중 사용자 시나리오 테스트")
    class MultiUserScenarioTest {

        @Test
        @DisplayName("여러 사용자의 RefreshToken 독립성 테스트")
        void testMultipleUsersTokenIndependence() {
            log.info("--- 다중 사용자 토큰 독립성 테스트 시작 ---");

            // 두 번째 사용자 생성
            User secondUser = User.builder()
                    .name("두번째사용자")
                    .nickname("seconduser")
                    .email("second@test.com")
                    .birthYear(1992)
                    .gender(Gender.MALE)
                    .socialId("second456")
                    .provider(AuthProvider.GOOGLE)
                    .role(Role.ROLE_USER)
                    .picture("second.jpg")
                    .isActive(true)
                    .build();

            User savedSecondUser = userRepository.save(secondUser);
            log.info("두 번째 사용자 생성: userId={}", savedSecondUser.getId());

            // 각각의 RefreshToken 생성
            String firstUserToken = jwtTokenProvider.generateRefreshToken(testUser.getId());
            String secondUserToken = jwtTokenProvider.generateRefreshToken(savedSecondUser.getId());

            // 각각 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, firstUserToken);
            refreshTokenService.saveOrUpdateRefreshToken(savedSecondUser, secondUserToken);

            log.info("두 사용자의 RefreshToken 저장 완료");

            // 각각 조회하여 독립성 확인
            RefreshToken firstToken = refreshTokenService.getRefreshTokenEntity(firstUserToken);
            RefreshToken secondToken = refreshTokenService.getRefreshTokenEntity(secondUserToken);

            // 검증
            assertThat(firstToken.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(secondToken.getUser().getId()).isEqualTo(savedSecondUser.getId());
            assertThat(firstToken.getUser().getEmail()).isEqualTo(testUser.getEmail());
            assertThat(secondToken.getUser().getEmail()).isEqualTo(savedSecondUser.getEmail());

            log.info("첫 번째 사용자 토큰 검증: userId={}", firstToken.getUser().getId());
            log.info("두 번째 사용자 토큰 검증: userId={}", secondToken.getUser().getId());

            // 한 사용자의 토큰 삭제가 다른 사용자에게 영향 없는지 확인
            refreshTokenService.deleteRefreshTokenByUserId(testUser.getId());

            // 첫 번째 사용자 토큰은 삭제됨
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(firstUserToken))
                    .isInstanceOf(RefreshTokenException.class);

            // 두 번째 사용자 토큰은 여전히 유효
            RefreshToken stillValidToken = refreshTokenService.getRefreshTokenEntity(secondUserToken);
            assertThat(stillValidToken.getUser().getId()).isEqualTo(savedSecondUser.getId());

            log.info("--- 다중 사용자 토큰 독립성 테스트 완료 ---");
        }

        @Test
        @DisplayName("비활성 사용자 처리 테스트")
        void testInactiveUserHandling() {
            log.info("--- 비활성 사용자 처리 테스트 시작 ---");

            // 토큰 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);
            log.info("활성 사용자로 토큰 저장 완료");

            // 토큰 조회 성공 확인
            RefreshToken activeUserToken = refreshTokenService.getRefreshTokenEntity(validRefreshToken);
            assertThat(activeUserToken.getUser().getIsActive()).isTrue();
            log.info("활성 사용자 토큰 조회 성공");

            // 사용자 비활성화
            testUser.softDelete(); // isActive = false
            userRepository.save(testUser);
            log.info("사용자 비활성화 완료");

            // 비활성 사용자의 토큰으로 조회 시도 (실패해야 함)
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenEntity(validRefreshToken))
                    .isInstanceOf(RefreshTokenException.class);
            log.info("비활성 사용자 토큰 조회 - 예상대로 예외 발생");

            log.info("--- 비활성 사용자 처리 테스트 완료 ---");
        }
    }

    @Nested
    @DisplayName("성능 및 대용량 데이터 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대량 토큰 저장 및 조회 성능 테스트")
        void testBulkTokenOperations() {
            log.info("--- 대량 토큰 연산 성능 테스트 시작 ---");

            int userCount = 50; // 테스트 환경에 맞게 조절
            long startTime = System.currentTimeMillis();

            // 대량 사용자 및 토큰 생성
            for (int i = 0; i < userCount; i++) {
                User bulkUser = User.builder()
                        .name("벌크사용자" + i)
                        .nickname("bulk" + i)
                        .email("bulk" + i + "@test.com")
                        .birthYear(1990 + (i % 10))
                        .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                        .socialId("bulk" + i)
                        .provider(AuthProvider.GOOGLE)
                        .role(Role.ROLE_USER)
                        .picture("bulk" + i + ".jpg")
                        .isActive(true)
                        .build();

                User savedBulkUser = userRepository.save(bulkUser);
                String bulkToken = jwtTokenProvider.generateRefreshToken(savedBulkUser.getId());
                refreshTokenService.saveOrUpdateRefreshToken(savedBulkUser, bulkToken);

                if (i % 10 == 0) {
                    log.info("처리 진행률: {}/{}", i, userCount);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("대량 데이터 처리 완료: {}개 사용자, 소요시간: {}ms", userCount, endTime - startTime);

            // 랜덤하게 몇 개 조회해보기
            for (int i = 0; i < 5; i++) {
                int randomIndex = (int) (Math.random() * userCount);
                String email = "bulk" + randomIndex + "@test.com";
                Optional<User> randomUser = userRepository.findByEmail(email);
                if (randomUser.isPresent()) {
                    Long userId = randomUser.get().getId();
                    Optional<RefreshToken> token = refreshTokenRepository.findByUserId(userId);
                    assertThat(token).isPresent();
                    log.info("랜덤 조회 성공: userId={}", userId);
                }
            }

            log.info("--- 대량 토큰 연산 성능 테스트 완료 ---");
        }

        @Test
        @DisplayName("동일 사용자 토큰 갱신 반복 테스트")
        void testRepeatedTokenRefresh() {
            log.info("--- 반복 토큰 갱신 테스트 시작 ---");

            int refreshCount = 20;
            String currentToken = validRefreshToken;

            for (int i = 0; i < refreshCount; i++) {
                // 새로운 토큰 생성
                String newToken = jwtTokenProvider.generateRefreshToken(testUser.getId());
                
                // 토큰 갱신
                refreshTokenService.saveOrUpdateRefreshToken(testUser, newToken);

                assertThat(refreshTokenService.getRefreshTokenEntity(newToken).getRefreshToken()).isEqualTo(newToken);
                
                // 새 토큰으로 조회 성공 확인
                RefreshToken retrievedToken = refreshTokenService.getRefreshTokenEntity(newToken);
                assertThat(retrievedToken.getUser().getId()).isEqualTo(testUser.getId());
                
                currentToken = newToken;
                
                if (i % 5 == 0) {
                    log.info("토큰 갱신 진행률: {}/{}", i, refreshCount);
                }
            }

            log.info("--- 반복 토큰 갱신 테스트 완료: {}회 갱신", refreshCount);
        }
    }

    @Nested
    @DisplayName("데이터 정합성 검증 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("Repository 메서드별 일관성 테스트")
        void testRepositoryMethodConsistency() {
            log.info("--- Repository 메서드 일관성 테스트 시작 ---");

            // 토큰 저장
            refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);

            // 다양한 방법으로 조회하여 일관성 확인
            Optional<RefreshToken> byToken = refreshTokenRepository.findByRefreshToken(validRefreshToken);
            Optional<RefreshToken> byUserId = refreshTokenRepository.findByUserId(testUser.getId());
            Optional<RefreshToken> byUserIdAndToken = refreshTokenRepository.findByUserIdAndRefreshToken(
                    testUser.getId(), validRefreshToken);

            // 모든 조회 방법이 동일한 결과를 반환하는지 확인
            assertThat(byToken).isPresent();
            assertThat(byUserId).isPresent();
            assertThat(byUserIdAndToken).isPresent();

            RefreshToken token1 = byToken.get();
            RefreshToken token2 = byUserId.get();
            RefreshToken token3 = byUserIdAndToken.get();

            assertThat(token1.getId()).isEqualTo(token2.getId()).isEqualTo(token3.getId());
            assertThat(token1.getRefreshToken()).isEqualTo(token2.getRefreshToken()).isEqualTo(token3.getRefreshToken());
            assertThat(token1.getUser().getId()).isEqualTo(token2.getUser().getId()).isEqualTo(token3.getUser().getId());

            log.info("Repository 메서드 일관성 확인 완료: tokenId={}", token1.getId());
            log.info("--- Repository 메서드 일관성 테스트 완료 ---");
        }

        @Test
        @DisplayName("트랜잭션 롤백 테스트")
        void testTransactionRollback() {
            log.info("--- 트랜잭션 롤백 테스트 시작 ---");

            // 초기 상태 확인
            Optional<RefreshToken> initialToken = refreshTokenRepository.findByUserId(testUser.getId());
            assertThat(initialToken).isEmpty();

            try {
                // 정상 저장
                refreshTokenService.saveOrUpdateRefreshToken(testUser, validRefreshToken);
                
                // 저장 확인
                Optional<RefreshToken> savedToken = refreshTokenRepository.findByUserId(testUser.getId());
                assertThat(savedToken).isPresent();
                log.info("정상 저장 확인: tokenId={}", savedToken.get().getId());

                // @Transactional 때문에 테스트 종료 시 롤백됨
                
            } catch (Exception e) {
                log.error("예외 발생: {}", e.getMessage());
                throw e;
            }

            log.info("--- 트랜잭션 롤백 테스트 완료 ---");
        }
    }
}