package com.example.cherrydan.oauth.repository;

import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);

   void deleteByToken(String tokenValue);
}
