package com.example.cherrydan.user.repository;

import com.example.cherrydan.oauth.domain.AuthProvider;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 활성 사용자만 조회하는 기본 메서드들
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    // 기존 메서드들 (소프트 삭제된 사용자도 포함)
    Optional<User> findByEmail(String email);
    
    // 이메일 존재 여부 확인 (활성 사용자만)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsActiveByEmail(@Param("email") String email);
    
    // 기존 메서드들 (하위 호환성)
    boolean existsByEmail(String email);

    // 1년 이전에 소프트 딜리트된 유저 조회
    @Query("SELECT u FROM User u WHERE u.isActive = false AND u.deletedAt < :oneYearAgo")
    List<User> findUsersDeletedBefore(@Param("oneYearAgo") LocalDateTime oneYearAgo);

}
