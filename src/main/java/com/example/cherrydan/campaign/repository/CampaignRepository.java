package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    // 기본 조회 메서드들
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.campaignType = :campaignType")
    Page<Campaign> findActiveByCampaignType(CampaignType campaignType, Pageable pageable);
    
    // SNS 플랫폼별 조회 메서드들 (정적 방식 유지 - 성능 최적화)
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND (c.blog = true OR c.clip = true)")
    Page<Campaign> findActiveByBlogTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND (c.insta = true OR c.reels = true)")
    Page<Campaign> findActiveByInstagramTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND (c.youtube = true OR c.shorts = true)")
    Page<Campaign> findActiveByYoutubeTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.tiktok = true")
    Page<Campaign> findActiveByTiktokTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.etc = true")
    Page<Campaign> findActiveByEtcTrue(Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.isActive = true")
    Page<Campaign> findActiveCampaigns(Pageable pageable);
} 