package com.example.cherrydan.campaign.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignSearchSyncService {

    private final JdbcTemplate jdbcTemplate;

    private static final String MAIN_TABLE = "campaigns_daily_search";
    private static final String TEMP_TABLE = "campaigns_daily_search_temp";
    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_DELAY = 18000L;
    private static final int LOCK_WAIT_TIMEOUT_SECONDS = 60;

    @Retryable(
        retryFor = {DataAccessException.class, RuntimeException.class},
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_BACKOFF_DELAY),
        recover = "recoverSync"
    )
    public void performSyncWithRetry(LocalDate targetDate) {
        log.info("동기화 시도 - 날짜: {}", targetDate);

        prepareTemporaryTable();
        int count = performSync(targetDate);
        swapTables();

        log.info("동기화 성공 - 날짜: {}, 건수: {}", targetDate, count);
    }

    private void prepareTemporaryTable() {
        try {
            jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s", TEMP_TABLE));

            String createTempTableSql = String.format("""
                CREATE TABLE %s (
                    id BIGINT PRIMARY KEY,
                    title VARCHAR(255),
                    created_at DATETIME,
                    FULLTEXT INDEX idx_title (title)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """, TEMP_TABLE);

            jdbcTemplate.execute(createTempTableSql);

            log.debug("임시 테이블 준비 완료: {}", TEMP_TABLE);
        } catch (Exception e) {
            log.error("임시 테이블 준비 실패", e);
            throw new RuntimeException("임시 테이블 준비 실패", e);
        }
    }

    private int performSync(LocalDate targetDate) {
        try {
            String insertSql = String.format("""
                INSERT INTO %s (id, title, created_at)
                SELECT id, title, created_at
                FROM campaigns
                WHERE is_active = 1
                  AND created_at >= ?
                  AND created_at < ?
                """, TEMP_TABLE);

            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime startOfNextDay = targetDate.plusDays(1).atStartOfDay();

            int count = jdbcTemplate.update(insertSql, startOfDay, startOfNextDay);
            log.debug("임시 테이블 데이터 삽입 완료 - 건수: {}", count);

            return count;
        } catch (Exception e) {
            log.error("데이터 동기화 실패 - 날짜: {}", targetDate, e);
            throw new RuntimeException("데이터 동기화 실패", e);
        }
    }

    private void swapTables() {
        try {
            jdbcTemplate.execute(String.format("SET SESSION lock_wait_timeout = %d", LOCK_WAIT_TIMEOUT_SECONDS));

            String renameSql = String.format("""
                RENAME TABLE
                    %s TO %s_old,
                    %s TO %s,
                    %s_old TO %s
                """,
                MAIN_TABLE, MAIN_TABLE,
                TEMP_TABLE, MAIN_TABLE,
                MAIN_TABLE, TEMP_TABLE
            );

            jdbcTemplate.execute(renameSql);
            log.debug("테이블 교체 완료: {} <-> {}", MAIN_TABLE, TEMP_TABLE);
        } catch (Exception e) {
            log.error("테이블 교체 실패", e);
            throw new RuntimeException("테이블 교체 실패", e);
        }
    }

    @Recover
    public void recoverSync(Exception e, LocalDate targetDate) {
        log.error("campaigns_daily_search 동기화 복구 메서드 호출 - {}회 재시도 모두 실패, 날짜: {}",
            RETRY_MAX_ATTEMPTS, targetDate, e);
    }
}