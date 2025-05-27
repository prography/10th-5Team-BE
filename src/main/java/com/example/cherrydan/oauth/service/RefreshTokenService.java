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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token.validity-in-days:14}")
    private long refreshTokenValidityInDays;

    public void saveRefreshToken(Long userId, String tokenValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        Instant expiryDate = Instant.now().plus(refreshTokenValidityInDays, ChronoUnit.DAYS);

        // 기존 토큰이 있으면 삭제 (한 사용자당 하나의 Refresh Token만 유지)
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // 새 토큰 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token 저장 완료: 사용자 ID = {}", userId);
    }

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String tokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(tokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("존재하지 않는 Refresh Token: {}", tokenValue);
            return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // 만료 여부 확인
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("만료된 Refresh Token: {}", tokenValue);
            deleteRefreshToken(tokenValue);
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByToken(tokenValue)
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .map(RefreshToken::getUser);
    }

    public void deleteRefreshToken(String tokenValue) {
        try {
            refreshTokenRepository.deleteByToken(tokenValue);
            log.info("Refresh Token 삭제 완료: 토큰 값 = {}", tokenValue);
        } catch (Exception e) {
            log.error("Refresh Token 삭제 실패: {}", e.getMessage());
            throw new RefreshTokenException();
        }
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        refreshTokenRepository.findByUser(user)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("사용자의 Refresh Token 삭제 완료: 사용자 ID = {}", userId);
                });
    }
}
