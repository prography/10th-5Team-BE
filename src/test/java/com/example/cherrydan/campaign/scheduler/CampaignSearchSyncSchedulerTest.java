package com.example.cherrydan.campaign.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("CampaignSearchSyncScheduler 통합테스트")
class CampaignSearchSyncSchedulerTest {

    @Autowired
    private CampaignSearchSyncScheduler campaignSearchSyncScheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("campaigns_daily_search 테이블 동기화가 성공적으로 수행된다")
    void syncDailySearchTable_Success() {
        // given
        Integer beforeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        // when
        campaignSearchSyncScheduler.syncDailySearchTable();

        // then
        Integer afterCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        assertThat(afterCount).isNotNull();
        assertThat(afterCount).isGreaterThanOrEqualTo(0);

        // 2025-11-29 데이터가 있다면 동기화 후 campaigns_daily_search에 데이터가 있어야 함
        Integer campaignCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns WHERE DATE(created_at) = '2025-11-29' AND is_active = 1",
            Integer.class
        );

        assertThat(afterCount).isEqualTo(campaignCount);
    }

    @Test
    @DisplayName("동기화된 데이터의 컬럼 값이 올바르게 복사된다")
    void syncDailySearchTable_CorrectDataCopied() {
        // when
        campaignSearchSyncScheduler.syncDailySearchTable();

        // then
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        if (count > 0) {
            jdbcTemplate.query(
                "SELECT id, title, created_at FROM campaigns_daily_search LIMIT 1",
                rs -> {
                    Long id = rs.getLong("id");
                    String title = rs.getString("title");

                    assertThat(id).isNotNull();
                    assertThat(title).isNotNull();
                    assertThat(title).isNotEmpty();
                }
            );
        }
    }

    @Test
    @DisplayName("TRUNCATE 후 INSERT가 정상적으로 수행된다")
    void syncDailySearchTable_TruncateAndInsert() {
        // given - 첫 번째 동기화
        campaignSearchSyncScheduler.syncDailySearchTable();
        Integer firstSyncCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        // when - 두 번째 동기화 (TRUNCATE 후 재생성)
        campaignSearchSyncScheduler.syncDailySearchTable();
        Integer secondSyncCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        // then - 두 번 동기화해도 같은 데이터만 들어있어야 함
        assertThat(firstSyncCount).isEqualTo(secondSyncCount);
    }

    @Test
    @DisplayName("is_active가 1인 캠페인만 동기화된다")
    void syncDailySearchTable_OnlyActiveTrue() {
        // when
        campaignSearchSyncScheduler.syncDailySearchTable();

        // then
        Integer dailySearchCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns_daily_search",
            Integer.class
        );

        Integer activeCampaignCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM campaigns WHERE DATE(created_at) = '2025-11-29' AND is_active = 1",
            Integer.class
        );

        assertThat(dailySearchCount).isEqualTo(activeCampaignCount);
    }
}

