package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.domain.RefreshToken;
import com.example.cherrydan.oauth.dto.RefreshTokenDTO;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cherrydan.user.repository.UserLoginHistoryRepository;
import com.example.cherrydan.user.domain.UserLoginHistory;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;
    private final UserLoginHistoryService userLoginHistoryService;

    @Transactional
    public TokenDTO refreshToken(RefreshTokenDTO refreshToken) {
        // RefreshToken 엔티티 검증 및 조회
        RefreshToken refreshTokenEntity = refreshTokenService.getRefreshTokenEntity(refreshToken.getRefreshToken());

        User user = refreshTokenEntity.getUser();

        // 보안을 위해 Access Token과 Refresh Token을 모두 새로 발급
        TokenDTO newTokens = tokenProvider.generateTokens(user.getId(), user.getEmail());

        // 기존 RefreshToken 엔티티의 토큰값만 업데이트
        refreshTokenEntity.setRefreshToken(newTokens.getRefreshToken());

        userLoginHistoryService.recordLogin(user.getId());

        log.info("토큰 갱신 완료: 사용자 ID = {}", user.getId());

        return newTokens;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshTokenByUserId(userId);
        log.info("사용자 {} 로그아웃 완료", userId);
    }
}

