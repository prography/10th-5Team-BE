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

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String tokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByRefreshToken(tokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            log.error("존재하지 않는 Refresh Token: {}", tokenValue);
            return false;
        }
        
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
