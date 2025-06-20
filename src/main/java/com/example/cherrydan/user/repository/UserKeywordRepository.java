package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUserId(Long userId);
    Optional<UserKeyword> findByUserIdAndKeyword(Long userId, String keyword);
    void deleteByUserIdAndKeyword(Long userId, String keyword);
    List<UserKeyword> findAllByKeyword(String keyword);
} 