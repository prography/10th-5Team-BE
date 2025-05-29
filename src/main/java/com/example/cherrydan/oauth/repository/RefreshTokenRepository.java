package com.example.cherrydan.oauth.repository;

import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUser(User user);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUserId(Long userId);
}
