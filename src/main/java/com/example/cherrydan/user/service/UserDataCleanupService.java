package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataCleanupService {

    private static final int DAYS_UNTIL_PERMANENT_DELETE = 365;

    private final UserRepository userRepository;
    private final UserRelatedDataDeletionService userRelatedDataDeletionService;

    public void cleanupExpiredUserData() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(DAYS_UNTIL_PERMANENT_DELETE);
        List<User> expiredUsers = userRepository.findUsersDeletedBefore(oneYearAgo);

        if (expiredUsers.isEmpty()) {
            log.info("1년 경과한 소프트 딜리트 유저가 없습니다");
            return;
        }

        log.info("{}명의 1년 경과 소프트 딜리트 유저 데이터 삭제를 시작합니다", expiredUsers.size());

        int successCount = 0;

        for (User user : expiredUsers) {
            try {
                userRelatedDataDeletionService.deleteUserRelatedData(user.getId());
                successCount++;
            } catch (Exception e) {
                log.error("유저 ID {}의 연관 데이터 삭제 실패: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("1년 경과 유저 데이터 삭제 완료 - 성공: {}건, 총: {}건", successCount, expiredUsers.size());
    }
}
