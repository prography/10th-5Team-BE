package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndCampaign(User user, Campaign campaign);
    List<Bookmark> findAllByUserAndIsActiveTrue(User user);
    Page<Bookmark> findAllByUserAndIsActiveTrue(User user, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
    void deleteByUserAndCampaign(User user, Campaign campaign);
    boolean existsByUserIdAndCampaignIdAndIsActiveTrue(Long userId, Long campaignId);
    List<Bookmark> findAllByUserIdAndIsActiveTrue(Long userId);
    
    // 원격에서 추가된 메서드들
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ReviewerAnnouncementGreaterThanEqual(Long userId, LocalDate date, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ReviewerAnnouncementLessThan(Long userId, LocalDate date, Pageable pageable);

    /**
     * 특정 날짜에 마감되는 활성 캠페인의 북마크들을 조회 (페치 조인 포함)
     */
    @Query("SELECT b FROM Bookmark b " +
           "JOIN FETCH b.campaign c " +
           "JOIN FETCH b.user u " +
           "WHERE b.isActive = true " +
           "AND c.isActive = true " +
           "AND c.applyEnd = :applyEndDate")
    List<Bookmark> findActiveBookmarksWithCampaignAndUserByApplyEndDate(@Param("applyEndDate") LocalDate applyEndDate);
} 