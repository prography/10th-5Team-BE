package com.example.cherrydan.user.service;

import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.inquiry.repository.InquiryRepository;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRelatedDataDeletionService {

    private final SnsConnectionRepository snsConnectionRepository;
    private final CampaignStatusRepository campaignStatusRepository;
    private final InquiryRepository inquiryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ActivityAlertRepository activityAlertRepository;
    private final KeywordCampaignAlertRepository keywordCampaignAlertRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserFCMTokenRepository userFCMTokenRepository;

    @Transactional
    public Long deleteUserRelatedData(Long userId) {
        try {
            log.info("유저 ID {}의 연관 데이터 삭제 시작", userId);

            snsConnectionRepository.deleteByUserId(userId);
            campaignStatusRepository.deleteByUserId(userId);
            inquiryRepository.deleteByUserId(userId);
            bookmarkRepository.deleteByUserId(userId);
            activityAlertRepository.deleteByUserId(userId);
            keywordCampaignAlertRepository.deleteByUserId(userId);
            refreshTokenRepository.deleteByUserId(userId);
            userFCMTokenRepository.deleteByUserId(userId);

            log.info("유저 ID {}의 모든 연관 데이터 삭제 완료", userId);
            return 1L;
        } catch (Exception e) {
            log.error("유저 ID {}의 데이터 삭제 중 오류 발생: {}", userId, e.getMessage());
            return 0L;
        }
    }
}