package com.example.cherrydan.activity.strategy;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.common.util.PagedAlertIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyResultAlertStrategy implements AlertStrategy {
    
    private final CampaignStatusRepository campaignStatusRepository;
    
    @Override
    public Iterator<ActivityAlert> generateAlertsIterator(LocalDate today) {
        return new PagedAlertIterator<>(
            page -> campaignStatusRepository.findByStatusAndReviewerAnnouncementDate(
                CampaignStatusType.APPLY, today, page),
            status -> ActivityAlert.builder()
                .user(status.getUser())
                .campaign(status.getCampaign())
                .alertType(ActivityAlertType.APPLY_RESULT_DDAY)
                .alertDate(today)
                .build()
        );
    }
}