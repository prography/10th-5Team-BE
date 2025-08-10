package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndCampaign(User user, Campaign campaign);
    List<Bookmark> findAllByUserAndIsActiveTrue(User user);
    Page<Bookmark> findAllByUserAndIsActiveTrue(User user, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
    void deleteByUserAndCampaign(User user, Campaign campaign);
    boolean existsByUserIdAndCampaignIdAndIsActiveTrue(Long userId, Long campaignId);
    List<Bookmark> findAllByUserIdAndIsActiveTrue(Long userId);
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ApplyEndGreaterThanEqual(Long userId, LocalDate date, Pageable pageable);
    Page<Bookmark> findByUserIdAndIsActiveTrueAndCampaign_ApplyEndLessThan(Long userId, LocalDate date, Pageable pageable);
} 