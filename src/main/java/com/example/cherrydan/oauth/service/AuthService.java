package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;

import com.example.cherrydan.oauth.dto.AccessTokenDTO;
import com.example.cherrydan.oauth.dto.RefreshTokenDTO;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cherrydan.user.repository.UserLoginHistoryRepository;
import com.example.cherrydan.user.domain.UserLoginHistory;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;
    private final UserLoginHistoryRepository userLoginHistoryRepository;

    @Transactional
    public TokenDTO refreshToken(RefreshTokenDTO refreshToken) {
        // 토큰 검증 로직을 AuthService로 이동
        User user = validateAndGetUser(refreshToken.getRefreshToken());

        // 보안을 위해 Access Token과 Refresh Token을 모두 새로 발급
        TokenDTO newTokens = tokenProvider.generateTokens(user.getId(), user.getEmail());

        saveLoginHistory(user.getId());

        // 새로운 Refresh Token을 DB에 저장
        refreshTokenService.saveOrUpdateRefreshToken(user.getId(), newTokens.getRefreshToken());

        log.info("토큰 갱신 완료: 사용자 ID = {}", user.getId());

        return newTokens;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshTokenByUserId(userId);
        log.info("사용자 {} 로그아웃 완료", userId);
    }



    // 공통 검증 로직
    private User validateAndGetUser(String refreshToken) {
        if (refreshToken == null) {
            throw new AuthException(ErrorMessage.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        return refreshTokenService.getUserByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }

    private void saveLoginHistory(Long id) {
        try{
            UserLoginHistory loginHistory = UserLoginHistory.builder()
                    .userId(id)
                    .loginDate(LocalDateTime.now())
                    .build();
            userLoginHistoryRepository.save(loginHistory);
        } catch (Exception e) {
            log.error("로그인 히스토리 저장 중 에러 발생: {}", e.getMessage());
            throw new AuthException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}

