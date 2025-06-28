package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    // 활성 사용자만 조회하는 메서드들
    @Query("SELECT uk FROM UserKeyword uk WHERE uk.user.id = :userId AND uk.user.isActive = true")
    List<UserKeyword> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uk FROM UserKeyword uk WHERE uk.user.id = :userId AND uk.keyword = :keyword AND uk.user.isActive = true")
    Optional<UserKeyword> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    @Query("SELECT uk FROM UserKeyword uk WHERE uk.id = :keywordId AND uk.user.id = :userId AND uk.user.isActive = true")
    Optional<UserKeyword> findByIdAndUserId(@Param("keywordId") Long keywordId, @Param("userId") Long userId);
    
    @Query("SELECT uk FROM UserKeyword uk WHERE uk.keyword = :keyword AND uk.user.isActive = true")
    List<UserKeyword> findAllByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(uk) > 0 FROM UserKeyword uk WHERE uk.user.id = :userId AND uk.keyword = :keyword AND uk.user.isActive = true")
    boolean existsByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    void deleteByUserIdAndKeyword(Long userId, String keyword);

    /**
     * 모든 사용자 키워드를 키워드별로 그룹핑하여 조회 (배치 최적화) - 활성 사용자만
     */
    @Query("SELECT uk FROM UserKeyword uk JOIN FETCH uk.user WHERE uk.user.isActive = true")
    List<UserKeyword> findAllWithUser();
    
    /**
     * 특정 키워드의 모든 사용자 조회 (배치 최적화) - 활성 사용자만
     */
    @Query("SELECT uk FROM UserKeyword uk JOIN FETCH uk.user WHERE uk.keyword = :keyword AND uk.user.isActive = true")
    List<UserKeyword> findByKeywordWithUser(@Param("keyword") String keyword);
} 