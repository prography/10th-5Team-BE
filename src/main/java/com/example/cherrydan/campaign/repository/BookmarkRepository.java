package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndCampaign(User user, Campaign campaign);
    List<Bookmark> findAllByUserAndIsActiveTrue(User user);
    Page<Bookmark> findAllByUserAndIsActiveTrue(User user, Pageable pageable);
    void deleteByUserAndCampaign(User user, Campaign campaign);
    Page<Bookmark> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
} 