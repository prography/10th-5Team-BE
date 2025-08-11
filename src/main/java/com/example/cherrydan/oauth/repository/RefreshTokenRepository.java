package com.example.cherrydan.oauth.repository;

import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // RefreshToken에 user 필드가 없으므로 단순 조회만 가능
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
