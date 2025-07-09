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
     * 기존 토큰이 있으면 업데이트, 없으면 새로 생성
     */
    public void saveOrUpdateRefreshToken(Long userId, String refreshTokenValue) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);
        
        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            RefreshToken token = existingToken.get();
            token.setRefreshToken(refreshTokenValue);
            log.info("사용자 ID {}의 기존 Refresh Token 업데이트", userId);
        } else {
            // 새 토큰 생성
            RefreshToken newToken = RefreshToken.builder()
                    .refreshToken(refreshTokenValue)
                    .user(user)
                    .build();
            RefreshToken savedToken = refreshTokenRepository.save(newToken);
            user.setRefreshToken(savedToken);
            log.info("사용자 ID {}의 새 Refresh Token 생성", userId);
        }
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
        jwtTokenProvider.validateToken(tokenValue);

        // 3. 리프레쉬 토큰 타입 확인
        if (!jwtTokenProvider.isRefreshToken(tokenValue)) {
            log.error("잘못된 토큰 타입 (Access Token이 전달됨): {}", tokenValue);
            return false;
        }

        log.info("Refresh Token 유효성 검사 통과: 사용자 ID = {}", 
                refreshTokenOpt.get().getUser().getId());
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByRefreshToken(tokenValue)
                .map(RefreshToken::getUser)
                .filter(user -> user.getIsActive()); // 활성 사용자만 반환
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        try {
            refreshTokenRepository.deleteByUserId(userId);
            log.info("사용자 ID {}의 Refresh Token 삭제 완료", userId);
        } catch (Exception e) {
            log.error("사용자 ID {}의 Refresh Token 삭제 실패: {}", userId, e.getMessage());
            throw new RefreshTokenException(ErrorMessage.REFRESH_TOKEN_DELETE_ERROR);
        }
    }
}
