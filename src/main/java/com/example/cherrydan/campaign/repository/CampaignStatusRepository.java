package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CampaignStatusRepository extends JpaRepository<CampaignStatus, Long> {
    List<CampaignStatus> findByCampaignAndIsActiveTrue(Campaign campaign);
    List<CampaignStatus> findByUserAndIsActiveTrue(User user);
    Optional<CampaignStatus> findByUserAndCampaignAndIsActiveTrue(User user, Campaign campaign);
    long countByCampaignAndStatusAndIsActiveTrue(Campaign campaign, CampaignStatusType status);
    Optional<CampaignStatus> findByUserAndCampaign(User user, Campaign campaign);
} 