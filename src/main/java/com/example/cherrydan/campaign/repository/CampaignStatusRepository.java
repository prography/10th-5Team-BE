package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.domain.CampaignType;
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
     * 특정 사용자의 여러 캠페인 상태를 벌크 업데이트 (isActive와 status 둘 다)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CampaignStatus cs SET cs.isActive = :isActive, cs.status = :status " +
           "WHERE cs.user = :user AND cs.campaign.id IN :campaignIds")
    void updateStatusAndActiveBatch(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds, 
                                   @Param("isActive") Boolean isActive, @Param("status") CampaignStatusType status);

    /**
     * 특정 사용자의 여러 캠페인 상태를 벌크 업데이트 (status만)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CampaignStatus cs SET cs.status = :status " +
           "WHERE cs.user = :user AND cs.campaign.id IN :campaignIds")
    void updateStatusOnlyBatch(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds, 
                              @Param("status") CampaignStatusType status);

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

    @Query("SELECT cs FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND (" +
           "  (:status = 0 AND c.reviewerAnnouncement IS NOT NULL AND c.reviewerAnnouncement <= :today) OR " +
           "  (:status = 1 AND c.contentSubmissionEnd IS NOT NULL AND c.contentSubmissionEnd <= :today) OR " +
           "  (:status = 3 AND c.contentSubmissionEnd IS NOT NULL AND c.contentSubmissionEnd <= :today) OR " +
           "  (:status = 4 AND c.resultAnnouncement IS NOT NULL AND c.resultAnnouncement <= :today)" +
           ") " +
           "ORDER BY " +
           "  CASE WHEN :status = 0 THEN c.reviewerAnnouncement END DESC, " +
           "  CASE WHEN :status = 1 THEN c.contentSubmissionEnd END DESC, " +
           "  CASE WHEN :status = 3 THEN c.contentSubmissionEnd END DESC, " +
           "  CASE WHEN :status = 4 THEN c.resultAnnouncement END DESC")
    List<CampaignStatus> findTop4ByUserAndStatusAndExpired(@Param("user") User user, @Param("status") CampaignStatusType status, @Param("today") LocalDate today);
    
    /**
     * 결과 발표일 APPLY 상태 조회 (페이징, 알림 허용 사용자만)
     */
    @Query("SELECT cs FROM CampaignStatus cs " +
           "JOIN FETCH cs.campaign c " +
           "JOIN FETCH cs.user u " +
           "WHERE cs.status = :status " +
           "AND c.reviewerAnnouncement = :date " +
           "AND cs.isActive = true " +
           "AND u.isActive = true " +
           "AND EXISTS (" +
           "  SELECT 1 FROM UserFCMToken ud " +
           "  WHERE ud.userId = u.id " +
           "  AND ud.isAllowed = true " +
           "  AND ud.isActive = true" +
           ")")
    Page<CampaignStatus> findByStatusAndReviewerAnnouncementDate(
        @Param("status") CampaignStatusType status,
        @Param("date") LocalDate date,
        Pageable pageable);
    
    /**
     * SELECTED + REGION 타입 방문 마감 조회 (페이징, 알림 허용 사용자만)
     */
    @Query("SELECT cs FROM CampaignStatus cs " +
           "JOIN FETCH cs.campaign c " +
           "JOIN FETCH cs.user u " +
           "WHERE cs.status = :selectedStatus " +
           "AND c.campaignType = :regionType " +
           "AND c.contentSubmissionEnd = :visitEndDate " +
           "AND cs.isActive = true " +
           "AND u.isActive = true " +
           "AND EXISTS (" +
           "  SELECT 1 FROM UserFCMToken ud " +
           "  WHERE ud.userId = u.id " +
           "  AND ud.isAllowed = true " +
           "  AND ud.isActive = true" +
           ")")
    Page<CampaignStatus> findSelectedRegionCampaignsByVisitEndDate(
        @Param("visitEndDate") LocalDate visitEndDate,
        Pageable pageable,
        @Param("selectedStatus") CampaignStatusType selectedStatus,
        @Param("regionType") CampaignType regionType);

    default Page<CampaignStatus> findSelectedRegionCampaignsByVisitEndDate(
        LocalDate visitEndDate, Pageable pageable) {
        return findSelectedRegionCampaignsByVisitEndDate(visitEndDate, pageable, 
            CampaignStatusType.SELECTED, CampaignType.REGION);
    }
    
    /**
     * REVIEWING 상태 리뷰 마감 조회 (페이징, 알림 허용 사용자만)
     */
    @Query("SELECT cs FROM CampaignStatus cs " +
           "JOIN FETCH cs.campaign c " +
           "JOIN FETCH cs.user u " +
           "WHERE cs.status = :reviewingStatus " +
           "AND c.contentSubmissionEnd = :reviewEndDate " +
           "AND cs.isActive = true " +
           "AND u.isActive = true " +
           "AND EXISTS (" +
           "  SELECT 1 FROM UserFCMToken ud " +
           "  WHERE ud.userId = u.id " +
           "  AND ud.isAllowed = true " +
           "  AND ud.isActive = true" +
           ")")
    Page<CampaignStatus> findReviewingCampaignsByReviewEndDate(
        @Param("reviewEndDate") LocalDate reviewEndDate,
        Pageable pageable,
        @Param("reviewingStatus") CampaignStatusType reviewingStatus);
    
    // 오버로딩 메서드 (파라미터 간소화)
    default Page<CampaignStatus> findReviewingCampaignsByReviewEndDate(
        LocalDate reviewEndDate, Pageable pageable) {
        return findReviewingCampaignsByReviewEndDate(reviewEndDate, pageable,
            CampaignStatusType.REVIEWING);
    }

    @Modifying
    @Query("DELETE FROM CampaignStatus cs WHERE cs.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
} 