package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.dto.AccessTokenDTO;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    @Transactional
    public AccessTokenDTO refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (refreshToken == null) {
            throw new AuthException(ErrorMessage.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        // 사용자 정보 조회
        Optional<User> userOpt = refreshTokenService.getUserByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            throw new UserException(ErrorMessage.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        
        // 새 Access Token 생성
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
        
        log.info("토큰 갱신 완료: 사용자 ID = {}", user.getId());
        
        // AccessTokenDTO 반환 (Refresh Token 없음)
        return new AccessTokenDTO(newAccessToken);
    }

    /**
     * 사용자를 위한 Access Token과 Refresh Token 생성 (로그인용)
     */
    @Transactional
    public TokenDTO generateTokens(User user) {
        // Access Token 생성
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());

        // Refresh Token 생성
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Refresh Token을 DB에 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        log.info("토큰 생성 완료: 사용자 ID = {}", user.getId());

        // TokenDTO 반환 (Access + Refresh Token 모두 포함)
        return new TokenDTO(accessToken, refreshToken);
    }

    /**
     * UserDetailsImpl로부터 Access Token과 Refresh Token 생성 (OAuth2용)
     */
    @Transactional
    public TokenDTO generateTokens(UserDetailsImpl userDetails) {
        // Access Token 생성
        String accessToken = tokenProvider.generateAccessToken(userDetails.getId(), userDetails.getEmail());
        
        // Refresh Token 생성
        String refreshToken = tokenProvider.generateRefreshToken(userDetails.getId());
        
        // Refresh Token을 DB에 저장
        refreshTokenService.saveRefreshToken(userDetails.getId(), refreshToken);
        
        log.info("OAuth2 토큰 생성 완료: 사용자 ID = {}, provider = {}", 
                userDetails.getId(), userDetails.getProvider());
        
        // TokenDTO 반환 (Access + Refresh Token 모두 포함)
        return new TokenDTO(accessToken, refreshToken);
    }
}
