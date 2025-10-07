package com.example.cherrydan.activity.service;

import com.example.cherrydan.activity.dto.UnreadAlertCountResponseDTO;
import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final ActivityAlertRepository activityAlertRepository;
    private final KeywordCampaignAlertRepository keywordCampaignAlertRepository;

    /**
     * 사용자의 미읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public UnreadAlertCountResponseDTO getUnreadAlertCount(Long userId) {
        long activityCount = activityAlertRepository.countUnreadByUserId(userId);
        long keywordCount = keywordCampaignAlertRepository.countUnreadByUserId(userId);

        return UnreadAlertCountResponseDTO.builder()
                .totalCount((int) (activityCount + keywordCount))
                .activityAlertCount((int) activityCount)
                .keywordAlertCount((int) keywordCount)
                .build();
    }
}
