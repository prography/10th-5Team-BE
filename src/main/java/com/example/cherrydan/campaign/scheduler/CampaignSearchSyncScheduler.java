package com.example.cherrydan.campaign.scheduler;

import com.example.cherrydan.campaign.service.CampaignSearchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignSearchSyncScheduler {

    private final CampaignSearchSyncService syncService;

    @Scheduled(cron = "0 30 6 * * ?", zone = "Asia/Seoul")
    public void syncDailySearchTable() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        log.info("campaigns_daily_search 동기화 시작 - 날짜: {}", yesterday);

        syncService.performSyncWithRetry(yesterday);
    }
}
