package com.example.cherrydan.oauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cherrydan.oauth.domain.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    Optional<RefreshToken> findByUserIdAndRefreshToken(Long userId, String refreshToken);
}
