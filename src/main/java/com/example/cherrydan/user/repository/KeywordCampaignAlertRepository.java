package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface KeywordCampaignAlertRepository extends JpaRepository<KeywordCampaignAlert, Long> {
    
    /**
     * 사용자의 키워드 알림 목록 조회
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.isActive = true ORDER BY kca.alertDate DESC")
    List<KeywordCampaignAlert> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);
    
    /**
     * 알림 미발송된 키워드 알림들 조회
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.isNotified = false AND kca.isActive = true")
    List<KeywordCampaignAlert> findUnnotifiedAlerts();

    /**
     * 사용자와 키워드의 알림 조회 (사용자-키워드 조합당 하나)
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.keyword = :keyword AND kca.isActive = true")
    KeywordCampaignAlert findByUserIdAndKeywordAndIsActiveTrue(@Param("userId") Long userId, @Param("keyword") String keyword);
} 