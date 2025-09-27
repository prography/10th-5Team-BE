package com.example.cherrydan.activity.strategy;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.common.util.CompositeAlertIterator;
import com.example.cherrydan.common.util.PagedAlertIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewingAlertStrategy implements AlertStrategy {
    
    private final CampaignStatusRepository campaignStatusRepository;
    
    @Override
    public Iterator<ActivityAlert> generateAlertsIterator(LocalDate today) {
        return new CompositeAlertIterator(
            new PagedAlertIterator<>(
                page -> campaignStatusRepository.findReviewingCampaignsByReviewEndDate(
                    today.plusDays(3), page),  // D-3
                status -> createAlert(status, ActivityAlertType.REVIEWING_DEADLINE_D3, today)
            ),
            new PagedAlertIterator<>(
                page -> campaignStatusRepository.findReviewingCampaignsByReviewEndDate(
                    today, page),  // D-Day
                status -> createAlert(status, ActivityAlertType.REVIEWING_DEADLINE_DDAY, today)
            )
        );
    }
    
    private ActivityAlert createAlert(CampaignStatus status, ActivityAlertType type, LocalDate date) {
        return ActivityAlert.builder()
            .user(status.getUser())
            .campaign(status.getCampaign())
            .alertType(type)
            .alertDate(date)
            .build();
    }
}