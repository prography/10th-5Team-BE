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
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.refreshToken = :refreshToken AND rt.user.isActive = true")
    Optional<RefreshToken> findByRefreshToken(@Param("refreshToken") String refreshToken);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.user.isActive = true")
    Optional<RefreshToken> findByUser(@Param("user") User user);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.user.isActive = true")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);
    
    void deleteByUser(User user);
    void deleteByUserId(Long userId);
}
