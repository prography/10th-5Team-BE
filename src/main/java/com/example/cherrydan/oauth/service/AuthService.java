package com.example.cherrydan.oauth.service;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
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

    @Transactional
    public TokenDTO refreshToken(String refreshToken) {
        try {
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
            
            // TokenDTO 반환
            return new TokenDTO(newAccessToken);
            
        } catch (AuthException | UserException e) {
            throw e; // 이미 적절한 커스텀 예외이므로 그대로 전파
        } catch (Exception e) {
            log.error("토큰 갱신 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }
    }
}
