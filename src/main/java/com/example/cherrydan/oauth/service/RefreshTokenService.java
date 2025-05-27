package com.example.capstone.oauth.service;

import com.example.capstone.common.annotation.LogExecutionTime;
import com.example.capstone.common.exception.BaseException;
import com.example.capstone.common.exception.ErrorMessage;
import com.example.capstone.oauth.model.RefreshToken;
import com.example.capstone.oauth.repository.RefreshTokenRepository;
import com.example.capstone.oauth.security.jwt.UserDetailsImpl;
import com.example.capstone.user.domain.User;
import com.example.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;

    @Transactional
    @LogExecutionTime
    public String createRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BaseException(ErrorMessage.USER_NOT_FOUND));
        
        /*
         * 참고: 리프레시 토큰 생성 방식
         * 현재는 UUID 방식으로 단순 문자열을 생성하고 DB에 저장합니다.
         * 액세스 토큰처럼 JWT 방식으로 생성할 수도 있지만, 보안상의 이유로 DB에 저장하는 방식을 선택했습니다.
         * 리프레시 토큰은 장기간 유효하기 때문에 서버에서 관리하고 필요시 무효화할 수 있어야 합니다.
         */
        
        // 토큰 생성
        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);

        if (existingTokenOpt.isPresent()) {
            // 기존 토큰이 있으면 업데이트
            RefreshToken existingToken = existingTokenOpt.get();
            existingToken.setToken(tokenValue);
            existingToken.setExpiryDate(expiryDate);
            refreshTokenRepository.save(existingToken);
            log.info("기존 리프레시 토큰 업데이트: 사용자 ID={}", user.getId());
            return tokenValue;
        } else {
            // 새 토큰 생성
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(tokenValue)
                    .expiryDate(expiryDate)
                    .build();

            refreshTokenRepository.save(refreshToken);
            log.info("새 리프레시 토큰 생성: 사용자 ID={}", user.getId());
            return tokenValue;
        }
    }

    @Transactional
    @LogExecutionTime
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new BaseException(ErrorMessage.AUTH_EXPIRED_REFRESH_TOKEN);
        }
        
        return token;
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BaseException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN));
    }
}