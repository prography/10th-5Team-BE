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

import java.time.LocalDate;
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

    // 키워드 맞춤형 캠페인 FULLTEXT 검색 (지정 날짜 기준 전일)
    @Query(value = """
        SELECT * FROM campaigns 
        WHERE MATCH(title) AGAINST(:keyword IN BOOLEAN MODE)
        AND is_active = 1 
        AND DATE(created_at) = DATE(:date - INTERVAL 1 DAY)
        ORDER BY created_at DESC
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Campaign> findByKeywordFullText(
        @Param("keyword") String keyword, 
        @Param("date") LocalDate date,
        @Param("offset") int offset, 
        @Param("limit") int limit
    );
    
    // 지정 날짜 기준 전일 생성된 키워드 맞춤형 캠페인 개수
    @Query(value = """
        SELECT COUNT(*) FROM campaigns 
        WHERE MATCH(title) AGAINST(:keyword IN BOOLEAN MODE)
        AND is_active = 1 
        AND DATE(created_at) = DATE(:date - INTERVAL 1 DAY)
        """, nativeQuery = true)
    long countByKeywordAndCreatedDate(@Param("keyword") String keyword, @Param("date") LocalDate date);
} 