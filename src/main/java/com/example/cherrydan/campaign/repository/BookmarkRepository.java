package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndCampaign(User user, Campaign campaign);
    List<Bookmark> findAllByUserAndIsActiveTrue(User user);
    Page<Bookmark> findAllByUserAndIsActiveTrue(User user, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
    void deleteByUserAndCampaign(User user, Campaign campaign);
    boolean existsByUserIdAndCampaignIdAndIsActiveTrue(Long userId, Long campaignId);
    List<Bookmark> findAllByUserIdAndIsActiveTrue(Long userId);
    
    // 원격에서 추가된 메서드들 (ReviewerAnnouncement -> ApplyEnd로 변경)
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ApplyEndGreaterThanEqual(Long userId, LocalDate date, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ApplyEndLessThan(Long userId, LocalDate date, Pageable pageable);

    /**
     * 특정 사용자의 여러 캠페인 ID에 대한 북마크를 조회
     */
    List<Bookmark> findByUserAndCampaignIdIn(User user, List<Long> campaignIds);

    /**
     * 특정 사용자가 북마크한 캠페인 ID들을 벌크 조회 (N+1 문제 해결)
     */
    @Query("SELECT b.campaign.id FROM Bookmark b " +
           "WHERE b.user.id = :userId " +
           "AND b.campaign.id IN :campaignIds " +
           "AND b.isActive = true")
    Set<Long> findBookmarkedCampaignIds(@Param("userId") Long userId, @Param("campaignIds") List<Long> campaignIds);

    /**
     * 특정 사용자의 여러 캠페인 북마크를 벌크 삭제
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.user = :user AND b.campaign.id IN :campaignIds")
    void deleteByUserAndCampaignIds(@Param("user") User user, @Param("campaignIds") List<Long> campaignIds);

    /**
     * 마감 D-1, D-day 북마크 조회 (페이징, 알림 허용 사용자만)
     */
    @Query("SELECT DISTINCT b FROM Bookmark b " +
           "JOIN FETCH b.campaign c " +
           "JOIN FETCH b.user u " +
           "JOIN UserFCMToken ud ON ud.userId = u.id " +
           "WHERE c.applyEnd = :applyEndDate " +
           "AND b.isActive = true " +
           "AND c.isActive = true " +
           "AND u.isActive = true " +
           "AND ud.isAllowed = true " +
           "AND ud.isActive = true")
    Page<Bookmark> findActiveBookmarksByApplyEndDate(@Param("applyEndDate") LocalDate applyEndDate, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 팝업용: 사용자의 활성 관심공고 전체 조회
     * 마감일이 오늘 이후인 것만 조회하여 마감일 순으로 정렬
     */
    @Query("SELECT b FROM Bookmark b " +
           "JOIN FETCH b.campaign c " +
           "JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId " +
           "AND b.isActive = true " +
           "AND c.isActive = true " +
           "AND c.applyEnd >= :today " +
           "ORDER BY c.applyEnd DESC")
    List<Bookmark> findActiveBookmarksByUserForPopup(@Param("userId") Long userId, @Param("today") LocalDate today);
} 