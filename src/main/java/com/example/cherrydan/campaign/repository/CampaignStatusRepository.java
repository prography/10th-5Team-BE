package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 특정 사용자의 여러 캠페인 상태를 벌크 업데이트
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CampaignStatus cs SET cs.isActive = :isActive, cs.status = :status " +
           "WHERE cs.user = :user AND cs.campaign.id IN :campaignIds")
    void updateStatusBatch(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds, 
                          @Param("isActive") Boolean isActive, @Param("status") CampaignStatusType status);

    /**
     * 특정 사용자의 여러 캠페인 상태를 벌크 삭제
     */
    @Modifying
    @Query("DELETE FROM CampaignStatus cs WHERE cs.user = :user AND cs.campaign.id IN :campaignIds")
    void deleteByUserAndCampaignIds(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds);

    /**
     * 벌크 업데이트된 상태들을 조회 (N+1 문제 방지를 위한 JOIN FETCH)
     */
    @Query("SELECT cs FROM CampaignStatus cs JOIN FETCH cs.campaign " +
           "WHERE cs.user = :user AND cs.campaign.id IN :campaignIds")
    List<CampaignStatus> findByUserAndCampaignIds(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds);
} 