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
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.campaignType = :campaignType")
    Page<Campaign> findByCampaignType(@Param("campaignType") CampaignType campaignType, Pageable pageable);
    
    // SNS 플랫폼별 조회 메서드들
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.blog = true")
    Page<Campaign> findByBlogTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND (c.insta = true OR c.reels = true)")
    Page<Campaign> findByInstagramTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND (c.youtube = true OR c.shorts = true)")
    Page<Campaign> findByYoutubeTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.tiktok = true")
    Page<Campaign> findByTiktokTrue(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.etc = true")
    Page<Campaign> findByEtcTrue(Pageable pageable);
    
    // 체험단 플랫폼별 조회 메서드들
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'cherivu'")
    Page<Campaign> findByExperiencePlatformCherivu(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'revu'")
    Page<Campaign> findByExperiencePlatformRevu(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'reviewnote'")
    Page<Campaign> findByExperiencePlatformReviewnote(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'dailyview'")
    Page<Campaign> findByExperiencePlatformDailyview(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = '4blog'")
    Page<Campaign> findByExperiencePlatformFourblog(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'popomon'")
    Page<Campaign> findByExperiencePlatformPopomon(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'dinnerqueen'")
    Page<Campaign> findByExperiencePlatformDinnerqueen(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'seoulouba'")
    Page<Campaign> findByExperiencePlatformSeoulouba(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'cometoplay'")
    Page<Campaign> findByExperiencePlatformCometoplay(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.sourceSite = 'gangnam'")
    Page<Campaign> findByExperiencePlatformGangnam(Pageable pageable);
} 