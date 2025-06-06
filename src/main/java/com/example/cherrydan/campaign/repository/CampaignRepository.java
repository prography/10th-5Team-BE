package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.Region;
import com.example.cherrydan.campaign.domain.RegionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 캠페인 Repository
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * 지역과 카테고리로 캠페인 검색
     * - "전체": 모든 지역
     * - "서울": main_region이 서울인 모든 캠페인  
     * - "강남/논현": detail_region이 강남/논현인 캠페인
     */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND (:mainRegion IS NULL OR c.mainRegion = :mainRegion) " +
           "AND (:detailRegion IS NULL OR c.detailRegion = :detailRegion) " +
           "AND (:regionCategory = 'ALL' OR c.regionCategory = :regionCategory) " +
           "ORDER BY c.created DESC")
    Page<Campaign> findByRegionAndCategory(
            @Param("mainRegion") Region mainRegion,
            @Param("detailRegion") Region detailRegion,
            @Param("regionCategory") String regionCategory,
            Pageable pageable);

    /**
     * 키워드 + 필터 조건으로 복합 검색
     */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.benefit) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:mainRegion IS NULL OR c.mainRegion = :mainRegion) " +
           "AND (:detailRegion IS NULL OR c.detailRegion = :detailRegion) " +
           "AND (:regionCategory IS NULL OR :regionCategory = 'ALL' OR c.regionCategory = :regionCategory) " +
           "AND (:product IS NULL OR (:product = 1 AND c.campaignType = 1)) " +
           "AND (:reporter IS NULL OR (:reporter = 1 AND c.campaignType = 2)) " +
           "AND c.socialPlatforms.youtube = :youtube " +
           "AND c.socialPlatforms.shorts = :shorts " +
           "AND c.socialPlatforms.insta = :insta " +
           "AND c.socialPlatforms.reels = :reels " +
           "AND c.socialPlatforms.blog = :blog " +
           "AND c.socialPlatforms.clip = :clip " +
           "AND c.socialPlatforms.tiktok = :tiktok " +
           "AND c.socialPlatforms.etc = :etc " +
           "AND c.applyEnd >= :deadlineStart " +
           "AND c.applyEnd <= :deadlineEnd " +
           "ORDER BY c.created DESC")
    Page<Campaign> findByKeywordAndFilters(
            @Param("keyword") String keyword,
            @Param("mainRegion") Region mainRegion,
            @Param("detailRegion") Region detailRegion,
            @Param("regionCategory") RegionCategory regionCategory,
            @Param("product") Integer product,
            @Param("reporter") Integer reporter,
            @Param("youtube") Integer youtube,
            @Param("shorts") Integer shorts,
            @Param("insta") Integer insta,
            @Param("reels") Integer reels,
            @Param("blog") Integer blog,
            @Param("clip") Integer clip,
            @Param("tiktok") Integer tiktok,
            @Param("etc") Integer etc,
            @Param("deadlineStart") LocalDate deadlineStart,
            @Param("deadlineEnd") LocalDate deadlineEnd,
            Pageable pageable);
}