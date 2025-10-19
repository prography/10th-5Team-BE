package com.example.cherrydan.user.service;

import com.example.cherrydan.activity.repository.ActivityAlertRepository;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.fcm.repository.UserFCMTokenRepository;
import com.example.cherrydan.inquiry.repository.InquiryRepository;
import com.example.cherrydan.oauth.repository.RefreshTokenRepository;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataCleanupService {

    private static final int DAYS_UNTIL_PERMANENT_DELETE = 365;

    private final UserRepository userRepository;
    private final SnsConnectionRepository snsConnectionRepository;
    private final CampaignStatusRepository campaignStatusRepository;
    private final InquiryRepository inquiryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ActivityAlertRepository activityAlertRepository;
    private final KeywordCampaignAlertRepository keywordCampaignAlertRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserFCMTokenRepository userFCMTokenRepository;

    @Transactional
    public void cleanupExpiredUserData() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(DAYS_UNTIL_PERMANENT_DELETE);
        List<User> expiredUsers = userRepository.findUsersDeletedBefore(oneYearAgo);

        if (expiredUsers.isEmpty()) {
            log.info("1년 경과한 소프트 딜리트 유저가 없습니다");
            return;
        }

        log.info("{}명의 1년 경과 소프트 딜리트 유저 데이터 삭제를 시작합니다", expiredUsers.size());

        int successCount = 0;
        int failCount = 0;

        for (User user : expiredUsers) {
            try {
                deleteUserRelatedData(user.getId());
                successCount++;
                log.info("유저 ID {}의 연관 데이터 삭제 완료 (이메일: {})", user.getId(), user.getMaskedEmail());
            } catch (Exception e) {
                failCount++;
                log.error("유저 ID {}의 연관 데이터 삭제 실패: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("1년 경과 유저 데이터 삭제 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }

    private void deleteUserRelatedData(Long userId) {
        log.debug("유저 ID {}의 연관 데이터 삭제 시작", userId);

        snsConnectionRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 SNS 연결 정보 삭제 완료", userId);

        campaignStatusRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 캠페인 상태 삭제 완료", userId);

        inquiryRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 문의 내역 삭제 완료", userId);

        bookmarkRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 북마크 삭제 완료", userId);

        activityAlertRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 활동 알림 삭제 완료", userId);

        keywordCampaignAlertRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 키워드 알림 삭제 완료", userId);

        refreshTokenRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 리프레시 토큰 삭제 완료", userId);

        userFCMTokenRepository.deleteByUserId(userId);
        log.debug("유저 ID {}의 FCM 토큰 삭제 완료", userId);

        log.info("유저 ID {}의 모든 연관 데이터 삭제 완료", userId);
    }
}
