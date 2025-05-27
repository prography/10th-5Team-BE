package com.example.capstone.oauth.service;

import com.example.capstone.common.annotation.LogExecutionTime;
import com.example.capstone.common.exception.BaseException;
import com.example.capstone.common.exception.ErrorMessage;
import com.example.capstone.oauth.dto.TokenDTO;
import com.example.capstone.oauth.model.RefreshToken;
import com.example.capstone.oauth.security.jwt.JwtTokenProvider;
import com.example.capstone.user.domain.User;
import com.example.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    @LogExecutionTime
    public TokenDTO refreshToken(String refreshToken) {
        try {
            // 리프레시 토큰 확인
            RefreshToken token = refreshTokenService.findByToken(refreshToken);
            
            // 토큰 만료 확인
            refreshTokenService.verifyExpiration(token);
            
            // 사용자 정보 조회
            User user = token.getUser();
            if (user == null) {
                throw new BaseException(ErrorMessage.USER_NOT_FOUND);
            }
            
            // 새 Access Token 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
            );
            
            String newAccessToken = tokenProvider.generateToken(authentication);
            
            // 반환할 TokenDTO 생성
            return TokenDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간
                    .build();
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            throw new BaseException(ErrorMessage.UNEXPECTED_ERROR);
        }
    }
}