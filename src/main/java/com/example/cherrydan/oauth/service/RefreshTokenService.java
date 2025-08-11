package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.RefreshTokenException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리프레쉬 토큰 저장 또는 업데이트
     * 데이터 정합성을 위해 기존 토큰 삭제 후 새로 생성
     */
    public void saveOrUpdateRefreshToken(Long userId, String refreshTokenValue) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        if (user.getRefreshToken() != null) {
            // 기존 토큰 값만 변경
            user.getRefreshToken().setRefreshToken(refreshTokenValue);
            log.info("사용자 ID {}의 기존 Refresh Token 업데이트", userId);
        } else {
            // 새 토큰 생성 (첫 로그인)
            RefreshToken newToken = RefreshToken.builder()
                    .refreshToken(refreshTokenValue)
                    .build();
            RefreshToken savedToken = refreshTokenRepository.save(newToken);
            user.setRefreshToken(savedToken);
            log.info("사용자 ID {}의 새 Refresh Token 생성", userId);
        }
        userRepository.save(user); // 변경감지로 모든 변경사항 저장
    }

    /**
     * 리프레쉬 토큰 완전한 유효성 검사
     * 1. 토큰 존재 여부 확인
     * 2. JWT 토큰 자체의 유효성 확인 (만료, 서명 등)
     * 3. 리프레쉬 토큰 타입 확인
     */
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String tokenValue) {
        // 1. DB에서 토큰 존재 여부 확인
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByRefreshToken(tokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            log.error("DB에 존재하지 않는 Refresh Token: {}", tokenValue);
            return false;
        }
        
        // 2. JWT 토큰 자체의 유효성 확인
        jwtTokenProvider.validateToken(tokenValue);

        // 3. 리프레쉬 토큰 타입 확인
        if (!jwtTokenProvider.isRefreshToken(tokenValue)) {
            log.error("잘못된 토큰 타입 (Access Token이 전달됨): {}", tokenValue);
            return false;
        }

        // 4. RefreshToken으로 User 조회 (User 활성 상태 확인)
        Optional<User> userOpt = getUserByRefreshToken(tokenValue);
        if (userOpt.isPresent()) {
            log.info("Refresh Token 유효성 검사 통과: 사용자 ID = {}", userOpt.get().getId());
            return true;
        } else {
            log.error("RefreshToken과 연결된 활성 사용자가 없습니다: {}", tokenValue);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByRefreshToken(String tokenValue) {
        // User 중심의 효율적인 조인 쿼리 사용
        return userRepository.findByRefreshTokenValue(tokenValue);
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        try {
            User user = userRepository.findActiveById(userId)
                    .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
            
            if (user.getRefreshToken() != null) {
                refreshTokenRepository.delete(user.getRefreshToken());
                user.setRefreshToken(null);
                userRepository.save(user);
                log.info("사용자 ID {}의 Refresh Token 삭제 완료", userId);
            } else {
                log.info("사용자 ID {}는 Refresh Token이 없습니다", userId);
            }
        } catch (Exception e) {
            log.error("사용자 ID {}의 Refresh Token 삭제 실패: {}", userId, e.getMessage());
            throw new RefreshTokenException(ErrorMessage.REFRESH_TOKEN_DELETE_ERROR);
        }
    }
}
