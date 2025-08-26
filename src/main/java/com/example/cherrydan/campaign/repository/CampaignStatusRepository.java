package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface CampaignStatusRepository extends JpaRepository<CampaignStatus, Long> {
    List<CampaignStatus> findByCampaignAndIsActiveTrue(Campaign campaign);
    List<CampaignStatus> findByUserAndIsActiveTrue(User user);
    Optional<CampaignStatus> findByUserAndCampaignAndIsActiveTrue(User user, Campaign campaign);
    long countByCampaignAndStatusAndIsActiveTrue(Campaign campaign, CampaignStatusType status);
    Optional<CampaignStatus> findByUserAndCampaign(User user, Campaign campaign);

    /**
     * 상태별 조회 - 캠페인 발표일(reviewerAnnouncement) 최신순 정렬
     */
    @Query("SELECT cs FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "ORDER BY c.reviewerAnnouncement DESC")
    Page<CampaignStatus> findByUserAndStatusAndIsActiveTrue(@Param("user") User user, @Param("status") CampaignStatusType status, Pageable pageable);
    long countByUserAndStatusAndIsActiveTrue(User user, CampaignStatusType status);
    
    /**
     * 사용자의 활동 알림 대상 캠페인들 조회 (3일 이내 마감)
     */
    @Query("SELECT cs FROM CampaignStatus cs WHERE cs.isActive = true AND cs.user.id = :userId")
    List<CampaignStatus> findActivityEligibleByUserId(@Param("userId") Long userId);
    
    /**
     * 알림 미발송된 활동 대상 캠페인들 조회
     */
    @Query("SELECT cs FROM CampaignStatus cs WHERE cs.isActive = true AND cs.activityNotified = false")
    List<CampaignStatus> findUnnotifiedActivityStatuses();
    
    /**
     * 특정 사용자의 활동 알림 대상 캠페인들 조회 (알림 발송용)
     */
    @Query("SELECT cs FROM CampaignStatus cs WHERE cs.isActive = true AND cs.user.id = :userId AND cs.activityNotified = false")
    List<CampaignStatus> findUnnotifiedActivityStatusesByUserId(@Param("userId") Long userId);

    /**
     * 사용자별로 isVisibleToUser = true인 활동 알림만 조회
     */
    @Query("SELECT cs FROM CampaignStatus cs WHERE cs.isActive = true AND cs.user.id = :userId AND cs.isVisibleToUser = true")
    List<CampaignStatus> findVisibleActivityByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자별로 isVisibleToUser = true인 활동 알림만 조회 (페이지네이션)
     */
    @Query("SELECT cs FROM CampaignStatus cs WHERE cs.isActive = true AND cs.user.id = :userId AND cs.isVisibleToUser = true")
    Page<CampaignStatus> findVisibleActivityByUserId(@Param("userId") Long userId, Pageable pageable);
} 