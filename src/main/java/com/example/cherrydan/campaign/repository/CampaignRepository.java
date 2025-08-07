package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    // 기본 조회 메서드들
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.campaignType = :campaignType")
    Page<Campaign> findActiveByCampaignType(CampaignType campaignType, Pageable pageable);
    
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

    @Query(value = "SELECT * FROM campaigns WHERE MATCH(title) AGAINST(:keyword IN BOOLEAN MODE) and is_active = 1 GROUP BY title ORDER BY competition_rate LIMIT 20", nativeQuery = true)
    List<Campaign> searchByTitleFullText(@Param("keyword") String keyword);

    // 키워드 맞춤형 캠페인 FULLTEXT 검색 (페이징용)
    @Query(value = """
        SELECT * FROM campaigns 
        WHERE is_active = 1 
        AND MATCH(title, benefit) AGAINST(:keyword IN BOOLEAN MODE)
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Campaign> findByKeywordFullText(
        @Param("keyword") String keyword, 
        @Param("offset") int offset, 
        @Param("limit") int limit
    );
    
    // 키워드 맞춤형 캠페인 FULLTEXT 검색 개수
    @Query(value = """
        SELECT COUNT(*) FROM campaigns 
        WHERE is_active = 1 
        AND MATCH(title, benefit) AGAINST(:keyword IN BOOLEAN MODE)
        """, nativeQuery = true)
    long countByKeywordFullText(@Param("keyword") String keyword);
} 