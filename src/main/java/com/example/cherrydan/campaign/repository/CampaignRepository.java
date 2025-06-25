package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
    
    // CampaignType enum의 ordinal 값을 사용해서 조회
    @Query("SELECT c FROM Campaign c WHERE c.campaignType = :campaignType")
    Page<Campaign> findByCampaignType(@Param("campaignType") CampaignType campaignType, Pageable pageable);
    
    // SNS 플랫폼별 조회 메서드들
    @Query("SELECT c FROM Campaign c WHERE c.blog = true")
    Page<Campaign> findByBlogTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.insta = true OR c.reels = true")
    Page<Campaign> findByInstagramTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.youtube = true OR c.shorts = true")
    Page<Campaign> findByYoutubeTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.tiktok = true")
    Page<Campaign> findByTiktokTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.etc = true")
    Page<Campaign> findByEtcTrue(Pageable pageable);
} 