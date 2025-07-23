package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.CampaignSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignSiteRepository extends JpaRepository<CampaignSite, Long> {
    List<CampaignSite> findAllByIsActiveTrueOrderByPriorityAsc();
} 