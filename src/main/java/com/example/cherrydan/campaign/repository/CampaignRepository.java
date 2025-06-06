package com.example.cherrydan.campaign.repository;

import com.example.cherrydan.campaign.domain.Campaign;
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
     * TODO: 실제 지역/카테고리 필드가 있을 때 구현
     */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND c.campaignType = 0 " +  // 지역 캠페인만
           "ORDER BY c.created DESC")
    Page<Campaign> findByRegionAndCategory(
            @Param("region") String region,
            @Param("category") String category,
            Pageable pageable);

    /**
     * 키워드 + 필터 조건으로 복합 검색
     * 캠페인 타입: null = 조건무시, 1 = 해당타입만
     * SNS 플랫폼: 0 = 불포함, 1 = 포함 조건
     * 날짜: Service에서 극단값으로 변환해서 전달
     */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.benefit) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:region1 IS NULL OR (:region1 = 1 AND c.campaignType = 0)) " +
           "AND (:region2 IS NULL OR (:region2 = 1 AND c.campaignType = 0)) " +
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
            @Param("region1") Integer region1,
            @Param("region2") Integer region2,
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