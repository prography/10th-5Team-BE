package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface KeywordCampaignAlertRepository extends JpaRepository<KeywordCampaignAlert, Long> {
    
    /**
     * 사용자의 키워드 알림 목록 조회
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.isVisibleToUser = true ORDER BY kca.alertDate DESC")
    List<KeywordCampaignAlert> findByUserIdAndIsVisibleToUserTrue(@Param("userId") Long userId);
    
    /**
     * 사용자의 키워드 알림 목록 조회 (페이지네이션)
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.isVisibleToUser = true ORDER BY kca.alertDate DESC")
    Page<KeywordCampaignAlert> findByUserIdAndIsVisibleToUserTrue(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 알림 미발송된 키워드 알림들 조회 (alertStage = 0인 발송 대기 중인 알림들)
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.alertStage = 0 AND kca.isVisibleToUser = true")
    List<KeywordCampaignAlert> findUnnotifiedAlerts();

    /**
     * 사용자와 키워드의 알림 조회 (사용자-키워드 조합당 하나)
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.keyword = :keyword AND kca.isVisibleToUser = true")
    KeywordCampaignAlert findByUserIdAndKeywordAndIsVisibleToUserTrue(@Param("userId") Long userId, @Param("keyword") String keyword);
} 