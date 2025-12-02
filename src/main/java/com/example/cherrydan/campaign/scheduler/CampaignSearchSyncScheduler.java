package com.example.cherrydan.campaign.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignSearchSyncScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 30 6 * * ?", zone = "Asia/Seoul")
    public void syncDailySearchTable() {
        try {
            LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

            log.info("campaigns_daily_search 동기화 시작 - 날짜: {}", yesterday);

            // 1. TRUNCATE
            jdbcTemplate.execute("TRUNCATE TABLE campaigns_daily_search");

            // 2. INSERT ... SELECT (한 방 쿼리, 인덱스 활용)
            String sql = """
                INSERT INTO campaigns_daily_search (id, title, created_at)
                SELECT id, title, created_at
                FROM campaigns
                WHERE is_active = 1
                  AND created_at >= ?
                  AND created_at < ?
                """;

            LocalDateTime startOfDay = yesterday.atStartOfDay();
            LocalDateTime startOfNextDay = yesterday.plusDays(1).atStartOfDay();

            int count = jdbcTemplate.update(sql, startOfDay, startOfNextDay);

            log.info("campaigns_daily_search 동기화 완료 - 날짜: {}, 건수: {}", yesterday, count);

        } catch (Exception e) {
            log.error("campaigns_daily_search 동기화 실패: {}", e.getMessage(), e);
        }
    }
}