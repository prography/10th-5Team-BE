package com.example.cherrydan.sns.repository;

import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnsConnectionRepository extends JpaRepository<SnsConnection, Long> {

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.platform = :platform AND sc.isActive = true")
    Optional<SnsConnection> findByUserAndPlatform(@Param("user") User user, @Param("platform") SnsPlatform platform);

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.isActive = true")
    List<SnsConnection> findByUser(@Param("user") User user);

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.platform = :platform")
    Optional<SnsConnection> findByUserAndPlatformIgnoreActive(@Param("user") User user, @Param("platform") SnsPlatform platform);

    boolean existsByUserAndPlatformAndIsActiveTrue(User user, SnsPlatform platform);

    List<SnsConnection> findAllByUser(User user);
} 