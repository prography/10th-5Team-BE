package com.example.cherrydan.activity.repository;

import com.example.cherrydan.activity.domain.ActivityAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityAlertRepository extends JpaRepository<ActivityAlert, Long> {
    
    /**
     * 사용자의 활동 알림 목록 조회 (페이지네이션)
     */
    @Query("SELECT aa FROM ActivityAlert aa WHERE aa.user.id = :userId AND aa.isVisibleToUser = true ORDER BY aa.alertDate DESC")
    Page<ActivityAlert> findByUserIdAndIsVisibleToUserTrue(@Param("userId") Long userId, Pageable pageable);


    /**
     * 당일 생성된 알림 미발송 활동 알림들 조회
     */
    @Query("SELECT aa FROM ActivityAlert aa WHERE aa.alertStage = 0 AND aa.isVisibleToUser = true AND aa.alertDate = :alertDate")
    List<ActivityAlert> findTodayUnnotifiedAlerts(@Param("alertDate") LocalDate alertDate);

    /**
     * 사용자와 캠페인으로 알림 존재 여부 확인
     */
    @Query("SELECT COUNT(aa) > 0 FROM ActivityAlert aa WHERE aa.user.id = :userId AND aa.campaign.id = :campaignId AND aa.isVisibleToUser = true")
    boolean existsByUserIdAndCampaignId(@Param("userId") Long userId, @Param("campaignId") Long campaignId);
}