package com.example.cherrydan.oauth.service;

import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class RefreshTokenServiceTest {
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceTest.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testRefreshTokenQueryWithNullUserId() {
        log.info("=== RefreshToken 쿼리 테스트 시작 ===");
        
        // 1. user_id가 NULL인 토큰 생성
        RefreshToken tokenWithNullUser = RefreshToken.builder()
                .refreshToken("test-token-null-user-123")
                .build(); // user 설정하지 않음 (user_id = NULL)
        
        RefreshToken savedToken = refreshTokenRepository.save(tokenWithNullUser);
        log.info("user_id가 NULL인 토큰 생성 완료: tokenId={}", savedToken.getId());
        
        // 2. findByRefreshToken 테스트 (rt.user.isActive 조건 포함)
        log.info("--- findByRefreshToken 테스트 ---");
        try {
            Optional<RefreshToken> result1 = refreshTokenRepository.findByRefreshToken("test-token-null-user-123");
            log.info("findByRefreshToken 결과: present={}", result1.isPresent());
            
            if (result1.isPresent()) {
                RefreshToken foundToken = result1.get();
                log.info("찾은 토큰 ID: {}", foundToken.getId());
                log.info("토큰의 user 객체: {}", foundToken.getUser());
                
                if (foundToken.getUser() != null) {
                    log.info("토큰의 user ID: {}", foundToken.getUser().getId());
                    log.info("토큰의 user isActive: {}", foundToken.getUser().getIsActive());
                }
            }
        } catch (Exception e) {
            log.error("findByRefreshToken 실행 중 에러 발생: {}", e.getMessage(), e);
        }
        
        // 3. findByUserId 테스트 (존재하지 않는 사용자 ID로)
        log.info("--- findByUserId 테스트 (존재하지 않는 사용자) ---");
        try {
            Optional<RefreshToken> result2 = refreshTokenRepository.findByUserId(99999L);
            log.info("findByUserId(99999) 결과: present={}", result2.isPresent());
        } catch (Exception e) {
            log.error("findByUserId 실행 중 에러 발생: {}", e.getMessage(), e);
        }
        
        // 4. 실제 사용자를 생성해서 테스트
        log.info("--- 실제 사용자로 테스트 ---");
        User testUser = User.builder()
                .email("test@test.com")
                .name("테스트 사용자")
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(testUser);
        log.info("테스트 사용자 생성 완료: userId={}", savedUser.getId());
        
        // 5. 사용자와 연결된 토큰 생성 (올바른 방식)
        RefreshToken tokenWithUser = RefreshToken.builder()
                .refreshToken("test-token-with-user-456")
                .user(savedUser)
                .build();
        
        RefreshToken savedTokenWithUser = refreshTokenRepository.save(tokenWithUser);
        log.info("사용자와 연결된 토큰 생성 완료: tokenId={}", savedTokenWithUser.getId());
        
        // 6. findByUserId 테스트 (실제 사용자 ID로)
        try {
            Optional<RefreshToken> result3 = refreshTokenRepository.findByUserId(savedUser.getId());
            log.info("findByUserId({}) 결과: present={}", savedUser.getId(), result3.isPresent());
            
            if (result3.isPresent()) {
                log.info("찾은 토큰 ID: {}", result3.get().getId());
            }
        } catch (Exception e) {
            log.error("findByUserId 실행 중 에러 발생: {}", e.getMessage(), e);
        }
        
        // 7. RefreshTokenService의 saveOrUpdateRefreshToken 메서드 테스트
        log.info("--- saveOrUpdateRefreshToken 메서드 테스트 ---");
        try {
            refreshTokenService.saveOrUpdateRefreshToken(savedUser.getId(), "new-refresh-token-789");
            log.info("saveOrUpdateRefreshToken 실행 완료");
        } catch (Exception e) {
            log.error("saveOrUpdateRefreshToken 실행 중 에러 발생: {}", e.getMessage(), e);
        }
        
        log.info("=== RefreshToken 쿼리 테스트 완료 ===");
    }
    
    @Test
    void testNullUserIdQuery() {
        log.info("=== NULL user_id 쿼리 동작 테스트 ===");
        
        // 1. user_id가 NULL인 토큰만 생성
        RefreshToken nullUserToken = RefreshToken.builder()
                .refreshToken("null-user-token")
                .build();
        refreshTokenRepository.save(nullUserToken);
        
        // 2. rt.user.isActive 조건이 있는 쿼리로 조회 시도
        log.info("rt.user.isActive 조건이 포함된 쿼리 실행...");
        Optional<RefreshToken> result = refreshTokenRepository.findByRefreshToken("null-user-token");
        
        log.info("쿼리 결과: present={}", result.isPresent());
        log.info("예상: user_id=NULL이므로 조인 실패로 empty 반환되어야 함");
        
        log.info("=== 테스트 완료 ===");
    }
}