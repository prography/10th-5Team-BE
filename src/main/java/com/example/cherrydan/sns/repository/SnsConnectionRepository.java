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

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.platform = :platform AND sc.isActive = true AND sc.user.isActive = true")
    Optional<SnsConnection> findByUserAndPlatform(@Param("user") User user, @Param("platform") SnsPlatform platform);

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.isActive = true AND sc.user.isActive = true")
    List<SnsConnection> findByUser(@Param("user") User user);

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.platform = :platform AND sc.user.isActive = true")
    Optional<SnsConnection> findByUserAndPlatformIgnoreActive(@Param("user") User user, @Param("platform") SnsPlatform platform);

    @Query("SELECT COUNT(sc) > 0 FROM SnsConnection sc WHERE sc.user = :user AND sc.platform = :platform AND sc.isActive = true AND sc.user.isActive = true")
    boolean existsByUserAndPlatformAndIsActiveTrue(@Param("user") User user, @Param("platform") SnsPlatform platform);

    @Query("SELECT sc FROM SnsConnection sc WHERE sc.user = :user AND sc.user.isActive = true")
    List<SnsConnection> findAllByUser(@Param("user") User user);
} 