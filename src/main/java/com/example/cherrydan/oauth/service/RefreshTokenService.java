package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.RefreshTokenException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
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

    public void saveRefreshToken(Long userId, String tokenValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        // 기존 토큰이 있으면 삭제 (한 사용자당 하나의 Refresh Token만 유지)
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);

        // 새 토큰 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .userId(userId)
                .refreshToken(tokenValue)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token 저장 완료: 사용자 ID = {}", userId);
    }

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String tokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByRefreshToken(tokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("존재하지 않는 Refresh Token: {}", tokenValue);
            return false;
        }
        
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByRefreshToken(tokenValue)
                .map(RefreshToken::getUser);
    }

    public void deleteRefreshToken(String tokenValue) {
        try {
            refreshTokenRepository.deleteByRefreshToken(tokenValue);
            log.info("Refresh Token 삭제 완료: 토큰 값 = {}", tokenValue);
        } catch (Exception e) {
            log.error("Refresh Token 삭제 실패: {}", e.getMessage());
            throw new RefreshTokenException(ErrorMessage.REFRESH_TOKEN_DELETE_ERROR);
        }
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("사용자의 Refresh Token 삭제 완료: 사용자 ID = {}", userId);
                });
    }

    /**
     * 사용자 ID로 리프레시 토큰을 찾는 메소드
     *
     * @param userId 사용자 ID
     * @return 리프레시 토큰 (존재하는 경우)
     */
    public Optional<String> findRefreshTokenByUserId(Long userId) {
        // DB에서 사용자 ID로 리프레시 토큰 엔티티를 조회
        Optional<RefreshToken> refreshTokenEntity = refreshTokenRepository.findByUserId(userId);

        // 엔티티가 존재하면 토큰 값을 반환, 없으면 빈 Optional 반환
        return refreshTokenEntity.map(RefreshToken::getRefreshToken);
    }
}
