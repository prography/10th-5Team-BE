package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.domain.CampaignStatusCase;
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

    /**
     * 새로운 케이스별 조회 메서드들
     */
    
    /**
     * appliedWaiting: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.reviewerAnnouncement >= :today " +
           "ORDER BY c.reviewerAnnouncement DESC")
    Page<Long> findIdsByUserAndAppliedWaiting(@Param("user") User user, 
                                                @Param("status") CampaignStatusType status,
                                                @Param("today") LocalDate today,
                                                Pageable pageable);
    
    /**
     * appliedCompleted: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.reviewerAnnouncement < :today " +
           "ORDER BY c.reviewerAnnouncement DESC")
    Page<Long> findIdsByUserAndAppliedCompleted(@Param("user") User user,
                                                @Param("status") CampaignStatusType status,
                                                @Param("today") LocalDate today,
                                                Pageable pageable);
    
    /**
     * resultSelected: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd >= :today " +
           "ORDER BY c.contentSubmissionEnd DESC")
    Page<Long> findIdsByUserAndResultSelected(@Param("user") User user,
                                              @Param("status") CampaignStatusType status,
                                              @Param("today") LocalDate today,
                                              Pageable pageable);
    
    /**
     * resultNotSelected: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd < :today " +
           "ORDER BY c.contentSubmissionEnd DESC")
    Page<Long> findIdsByUserAndResultNotSelected(@Param("user") User user,
                                                  @Param("status") CampaignStatusType status,
                                                  @Param("today") LocalDate today,
                                                  Pageable pageable);
    
    /**
     * reviewInProgress: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd >= :today " +
           "ORDER BY c.contentSubmissionEnd DESC")
    Page<Long> findIdsByUserAndReviewInProgress(@Param("user") User user,
                                                @Param("status") CampaignStatusType status,
                                                @Param("today") LocalDate today,
                                                Pageable pageable);
    
    /**
     * reviewCompleted: ID만 페이징 조회 (1단계)
     */
    @Query("SELECT cs.id FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "ORDER BY c.resultAnnouncement DESC")
    Page<Long> findIdsByUserAndReviewCompleted(@Param("user") User user,
                                               @Param("status") CampaignStatusType status,
                                               Pageable pageable);
    
    /**
     * ID 리스트로 CampaignStatus 조회 (2단계) - N+1 문제 방지를 위해 JOIN FETCH 사용
     * ID 순서를 유지하기 위해 순서대로 조회
     */
    @Query("SELECT cs FROM CampaignStatus cs " +
           "JOIN FETCH cs.campaign c " +
           "JOIN FETCH cs.user u " +
           "WHERE cs.id IN :ids")
    List<CampaignStatus> findByIdsWithFetch(@Param("ids") List<Long> ids);

    /**
     * 새로운 케이스별 카운트 메서드들
     */
    
    /**
     * appliedWaiting 카운트: APPLY 상태 + reviewer_announcement >= 오늘
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.reviewerAnnouncement >= :today")
    long countByUserAndAppliedWaiting(@Param("user") User user,
                                      @Param("status") CampaignStatusType status,
                                      @Param("today") LocalDate today);
    
    /**
     * appliedCompleted 카운트: APPLY 상태 + reviewer_announcement < 오늘
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.reviewerAnnouncement < :today")
    long countByUserAndAppliedCompleted(@Param("user") User user,
                                        @Param("status") CampaignStatusType status,
                                        @Param("today") LocalDate today);
    
    /**
     * resultSelected 카운트: SELECTED 상태 + content_submission_end >= 오늘
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd >= :today")
    long countByUserAndResultSelected(@Param("user") User user,
                                      @Param("status") CampaignStatusType status,
                                      @Param("today") LocalDate today);
    
    /**
     * resultNotSelected 카운트: NOT_SELECTED 상태 + content_submission_end < 오늘
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd < :today")
    long countByUserAndResultNotSelected(@Param("user") User user,
                                        @Param("status") CampaignStatusType status,
                                        @Param("today") LocalDate today);
    
    /**
     * reviewInProgress 카운트: REVIEWING 상태 + content_submission_end >= 오늘
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs JOIN cs.campaign c " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true " +
           "AND c.contentSubmissionEnd >= :today")
    long countByUserAndReviewInProgress(@Param("user") User user,
                                       @Param("status") CampaignStatusType status,
                                       @Param("today") LocalDate today);
    
    /**
     * reviewCompleted 카운트: ENDED 상태
     */
    @Query("SELECT COUNT(cs) FROM CampaignStatus cs " +
           "WHERE cs.user = :user AND cs.status = :status AND cs.isActive = true")
    long countByUserAndReviewCompleted(@Param("user") User user,
                                      @Param("status") CampaignStatusType status);
} 