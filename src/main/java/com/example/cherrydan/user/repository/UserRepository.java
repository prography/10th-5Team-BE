package com.example.cherrydan.user.repository;

import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 활성 사용자만 조회하는 기본 메서드들
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.socialId = :socialId AND u.isActive = true")
    Optional<User> findActiveByProviderAndSocialId(@Param("provider") AuthProvider provider, @Param("socialId") String socialId);
    
    // 기존 메서드들 (소프트 삭제된 사용자도 포함)
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderAndSocialId(AuthProvider provider, String socialId);
    
    // 이메일 존재 여부 확인 (활성 사용자만)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsActiveByEmail(@Param("email") String email);
    
    // 특정 이메일이 다른 OAuth 제공자로 이미 가입되었는지 확인 (활성 사용자만)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.provider != :provider AND u.isActive = true")
    boolean existsActiveByEmailAndProviderNot(@Param("email") String email, @Param("provider") AuthProvider provider);
    
    // 기존 메서드들 (하위 호환성)
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndProviderNot(String email, AuthProvider provider);
    
    // 모든 활성 사용자 조회
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActive();
    
    // RefreshToken 값으로 User 조회 (효율적인 조인)
    @Query("SELECT u FROM User u JOIN u.refreshToken rt WHERE rt.refreshToken = :tokenValue AND u.isActive = true")
    Optional<User> findByRefreshTokenValue(@Param("tokenValue") String tokenValue);
}
