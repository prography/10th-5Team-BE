package com.example.cherrydan.user.repository;

import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);
    
    // 특정 OAuth 제공자와 제공자 ID로 사용자 조회
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    // 이메일 존재 여부 확인 (중복 회원가입 방지)
    boolean existsByEmail(String email);
    
    // 특정 이메일이 다른 OAuth 제공자로 이미 가입되었는지 확인
    boolean existsByEmailAndProviderNot(String email, AuthProvider provider);
}
