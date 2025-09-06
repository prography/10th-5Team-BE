package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.RefreshTokenException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.domain.RefreshToken;
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
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리프레쉬 토큰 저장 또는 업데이트
     * 데이터 정합성을 위해 기존 토큰 삭제 후 새로 생성
     */
    public void saveOrUpdateRefreshToken(User user, String refreshTokenValue) {
        // 기존 토큰 확인 (userId로만 조회)
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());
        if (existingToken.isPresent()) {
            // 기존 토큰의 값만 업데이트
            existingToken.get().setRefreshToken(refreshTokenValue);
            log.info("사용자 ID {}의 기존 Refresh Token 업데이트", user.getId());
            return;
        }

        // 새 토큰 생성 (첫 로그인 등)
        RefreshToken newToken = RefreshToken.builder()
                .refreshToken(refreshTokenValue)
                .user(user)
                .build();
        refreshTokenRepository.save(newToken);
        log.info("사용자 ID {}의 새 Refresh Token 생성", user.getId());
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshTokenEntity(String tokenValue) {
        // 1. JWT 토큰 자체의 유효성 확인 (만료, 서명 등)
        jwtTokenProvider.validateToken(tokenValue);

        // 2. 리프레쉬 토큰 타입 확인
        if (!jwtTokenProvider.isRefreshToken(tokenValue)) {
            log.error("잘못된 토큰 타입 (Access Token이 전달됨): {}", tokenValue);
            throw new RefreshTokenException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        // 3. JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(tokenValue);

        // 4. userId와 tokenValue로 RefreshToken 조회
        RefreshToken refreshToken = refreshTokenRepository.findByUserIdAndRefreshToken(userId, tokenValue)
                .orElseThrow(() -> {
                    log.error("유효하지 않은 Refresh Token");
                    return new RefreshTokenException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
                });

        // 5. 활성 사용자 확인
        if (!refreshToken.getUser().getIsActive()) {
            log.error("비활성 사용자: {}", userId);
            throw new RefreshTokenException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        return refreshToken;
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
