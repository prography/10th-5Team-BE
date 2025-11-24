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

    /**
     * 키워드 맞춤형 캠페인 FULLTEXT 검색 (지정 날짜 기준 전일)
     *
     * @deprecated Simple LIKE 방식(findByKeywordSimpleLike)으로 대체되었습니다.
     *             성능: 희귀 키워드 280ms → 20ms, 흔한 키워드 180ms → 43ms
     *             STRAIGHT_JOIN 방식은 복잡하고 FULLTEXT 인덱스 활용도가 낮습니다.
     */
    @Deprecated
    @Query(value = """
        SELECT
          c.*
        FROM (
          SELECT
            id, created_at
          FROM campaigns FORCE INDEX(campaigns_created_at_is_active_IDX)
          WHERE created_at >=  DATE(:date - INTERVAL 1 DAY) AND created_at < DATE(:date)
            AND is_active = 1
        ) AS filtered
        STRAIGHT_JOIN campaigns AS c ON c.id = filtered.id
        WHERE MATCH(c.title) AGAINST(:keyword IN BOOLEAN MODE)
        ORDER BY filtered.created_at
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Campaign> findByKeywordFullText(
        @Param("keyword") String keyword,
        @Param("date") LocalDate date,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 지정 날짜 기준 전일 생성된 키워드 맞춤형 캠페인 개수
     *
     * @deprecated Simple LIKE 방식(countByKeywordSimpleLike)으로 대체되었습니다.
     */
    @Deprecated
    @Query(value = """
        SELECT
          COUNT(*)
        FROM (
          SELECT
            id
          FROM campaigns FORCE INDEX(campaigns_created_at_is_active_IDX)
          WHERE created_at >=  DATE(:date - INTERVAL 1 DAY) AND created_at < DATE(:date)
            AND is_active = 1
        ) AS filtered
        STRAIGHT_JOIN campaigns AS c ON c.id = filtered.id
        WHERE MATCH(c.title) AGAINST(:keyword IN BOOLEAN MODE)
        """, nativeQuery = true)
    long countByKeywordAndCreatedDate(@Param("keyword") String keyword, @Param("date") LocalDate date);

    // 단순 LIKE (가장 빠른 방식)
    @Query(value = """
        SELECT c.*
        FROM campaigns c
        WHERE c.is_active = 1
          AND c.created_at >= DATE(:date - INTERVAL 1 DAY)
          AND c.created_at < DATE(:date)
          AND c.title LIKE CONCAT('%', :keyword, '%')
        ORDER BY c.created_at
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Campaign> findByKeywordSimpleLike(
        @Param("keyword") String keyword,
        @Param("date") LocalDate date,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM campaigns c
        WHERE AND c.is_active = 1
          AND c.created_at >= DATE(:date - INTERVAL 1 DAY)
          AND c.created_at < DATE(:date)
          AND c.title LIKE CONCAT('%', :keyword, '%')
        """, nativeQuery = true)
    long countByKeywordSimpleLike(@Param("keyword") String keyword, @Param("date") LocalDate date);

    @Query(value = """
        SELECT c.*
        FROM campaigns_daily_search cds
        INNER JOIN campaigns c ON c.id = cds.id
        WHERE MATCH(cds.title) AGAINST(:keyword IN BOOLEAN MODE)
        ORDER BY cds.created_at
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Campaign> searchDailyCampaignsByFulltext(
        @Param("keyword") String keyword,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM campaigns_daily_search cds
        WHERE MATCH(cds.title) AGAINST(:keyword IN BOOLEAN MODE)
        """, nativeQuery = true)
    long countDailyCampaignsByFulltext(@Param("keyword") String keyword);
} 