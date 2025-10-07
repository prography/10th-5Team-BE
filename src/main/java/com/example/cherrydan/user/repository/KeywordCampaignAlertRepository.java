package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface KeywordCampaignAlertRepository extends JpaRepository<KeywordCampaignAlert, Long> {
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
     * 당일 생성된 알림 미발송 키워드 알림들 조회
     */
    @Query("SELECT kca FROM KeywordCampaignAlert kca WHERE kca.alertStage = 0 AND kca.isVisibleToUser = true AND kca.alertDate = :alertDate")
    List<KeywordCampaignAlert> findTodayUnnotifiedAlerts(@Param("alertDate") LocalDate alertDate);
    
    /**
     * 사용자가 키워드로 캠페인 조회시 해당 키워드 알림을 읽음 처리
     */
    @Query("UPDATE KeywordCampaignAlert kca SET kca.isRead = true WHERE kca.user.id = :userId AND kca.alertDate = :date AND kca.keyword = :keyword AND kca.isRead = false")
    @Modifying
    void markAsReadByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("date") LocalDate date);

    /**
     * 사용자의 미읽은 키워드 알림 개수 조회
     */
    @Query("SELECT COUNT(kca) FROM KeywordCampaignAlert kca WHERE kca.user.id = :userId AND kca.isRead = false AND kca.isVisibleToUser = true")
    Long countUnreadByUserId(@Param("userId") Long userId);
} 