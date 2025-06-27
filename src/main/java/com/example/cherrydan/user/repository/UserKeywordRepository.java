package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUserId(Long userId);
    Optional<UserKeyword> findByUserIdAndKeyword(Long userId, String keyword);
    void deleteByUserIdAndKeyword(Long userId, String keyword);
    List<UserKeyword> findAllByKeyword(String keyword);

    /**
     * 모든 사용자 키워드를 키워드별로 그룹핑하여 조회 (배치 최적화)
     */
    @Query("SELECT uk FROM UserKeyword uk JOIN FETCH uk.user")
    List<UserKeyword> findAllWithUser();
    
    /**
     * 특정 키워드의 모든 사용자 조회 (배치 최적화)
     */
    @Query("SELECT uk FROM UserKeyword uk JOIN FETCH uk.user WHERE uk.keyword = :keyword")
    List<UserKeyword> findByKeywordWithUser(@Param("keyword") String keyword);

    boolean existsByUserIdAndKeyword(Long userId, String keyword);
} 