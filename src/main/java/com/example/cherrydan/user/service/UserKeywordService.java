package com.example.cherrydan.user.service;

import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.repository.UserKeywordRepository;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.exception.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import com.example.cherrydan.user.dto.KeywordCampaignAlertResponseDTO;
import com.example.cherrydan.campaign.service.CampaignServiceImpl;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKeywordService {
    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;
    private final KeywordCampaignAlertRepository keywordAlertRepository;
    private final CampaignServiceImpl campaignService;
    private final KeywordProcessingService keywordProcessingService;

    @Transactional
    public void addKeyword(Long userId, String keyword) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        if (userKeywordRepository.findByUserIdAndKeyword(userId, keyword).isPresent()) {
            throw new UserException(ErrorMessage.USER_KEYWORD_ALREADY_EXISTS);
        }

        long keywordCount = userKeywordRepository.countByUserId(userId);
        if (keywordCount >= 5) {
            throw new UserException(ErrorMessage.USER_KEYWORD_LIMIT_EXCEEDED);
        }

        userKeywordRepository.save(UserKeyword.builder().user(user).keyword(keyword).build());
    }

    @Transactional(readOnly = true)
    public List<UserKeyword> getKeywords(Long userId) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        return userKeywordRepository.findByUserId(userId);
    }

    @Transactional
    public void removeKeywordById(Long userId, Long keywordId) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        // 키워드가 해당 사용자의 것인지 확인 후 삭제
        UserKeyword userKeyword = userKeywordRepository.findByIdAndUserId(keywordId, userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_KEYWORD_NOT_FOUND));
        
        userKeywordRepository.delete(userKeyword);
    }

    /**
     * 키워드 맞춤 캠페인 알림 대상 업데이트 (10개와 100개를 넘는 순간에만)
     * 새벽 7시 30분에 배치 처리로 실행
     */
    @Transactional
    public void updateKeywordCampaignAlerts() {
        log.info("키워드 맞춤 캠페인 알림 배치 업데이트 시작");
        
        // 키워드별로 그룹핑해서 효율적으로 처리
        Map<String, List<UserKeyword>> keywordGroups = userKeywordRepository.findAllWithUser()
                .stream()
                .collect(Collectors.groupingBy(UserKeyword::getKeyword));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        
        // 모든 키워드에 대해 비동기 처리 시작 (예외 처리 포함)
        List<CompletableFuture<List<KeywordCampaignAlert>>> safeFutures = keywordGroups.entrySet().stream()
            .map(entry -> keywordProcessingService.processKeywordAsync(entry.getKey(), entry.getValue(), today) // 어제 날짜로 고정
                .exceptionally(throwable -> {
                    log.error("키워드 '{}' 처리 실패: {}", entry.getKey(), throwable.getMessage());
                    return new ArrayList<>(); // 실패시 빈 리스트 반환
                }))
            .toList();
        
        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(safeFutures.toArray(new CompletableFuture[0])).join();
        
        // 결과 수집
        List<KeywordCampaignAlert> alertsToSave = safeFutures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // 벌크 저장으로 성능 최적화
        if (!alertsToSave.isEmpty()) {
            keywordAlertRepository.saveAll(alertsToSave);
            log.info("벌크 저장 완료: {}개 알림", alertsToSave.size());
        }
        
        log.info("키워드 맞춤 캠페인 알림 배치 업데이트 완료: 총 {}개 알림 생성", alertsToSave.size());
    }


    /**
     * 키워드 맞춤 알림 발송 (범위별 배치 발송으로 최적화)
     */
    @Transactional
    public void sendKeywordCampaignNotifications() {
        log.info("키워드 맞춤 알림 발송 시작");
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<KeywordCampaignAlert> unnotifiedAlerts = keywordAlertRepository.findTodayUnnotifiedAlerts(today);
        
        if (unnotifiedAlerts.isEmpty()) {
            log.info("발송할 키워드 알림이 없습니다.");
            return;
        }
        
        // 키워드별로 그룹핑 (같은 키워드 = 같은 메시지)
        Map<String, List<KeywordCampaignAlert>> groupedByKeyword = unnotifiedAlerts.stream()
                .collect(Collectors.groupingBy(KeywordCampaignAlert::getKeyword));
        
        // 키워드별 병렬 알림 발송 (예외 처리 포함)
        List<CompletableFuture<List<KeywordCampaignAlert>>> futures = groupedByKeyword.entrySet().stream()
            .map(entry -> keywordProcessingService.sendKeywordNotificationAsync(entry.getKey(), entry.getValue())
                .exceptionally(throwable -> {
                    log.error("키워드 '{}' 알림 발송 실패: {}", entry.getKey(), throwable.getMessage());
                    return new ArrayList<>(); // 실패시 빈 리스트 반환
                }))
            .toList();
        
        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 결과 수집
        List<KeywordCampaignAlert> allAlertsToUpdate = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        int totalSentCount = allAlertsToUpdate.size();
        
        // 성공한 알림들 상태 업데이트
        if (totalSentCount > 0) {
            try {
                allAlertsToUpdate.forEach(alert -> 
                    alert.markAsNotified());
                keywordAlertRepository.saveAll(allAlertsToUpdate);
                
                log.info("알림 상태 벌크 업데이트 완료: 성공한 알림 {}개", allAlertsToUpdate.size());
                
            } catch (Exception e) {
                log.error("알림 상태 업데이트 실패: {}", e.getMessage());
            }
        }
        
        log.info("키워드 맞춤 개별 알림 발송 완료: 총 {}건 발송", totalSentCount);
    }

    /**
     * 사용자의 키워드 알림 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<KeywordCampaignAlertResponseDTO> getUserKeywordAlerts(Long userId, Pageable pageable) {
        return keywordAlertRepository.findByUserIdAndIsVisibleToUserTrue(userId, pageable)
                .map(KeywordCampaignAlertResponseDTO::fromEntity);
    }

    /**
     * 특정 키워드로 맞춤형 캠페인 목록 조회
     */
    @Transactional
    public Page<CampaignResponseDTO> getPersonalizedCampaignsByKeyword(String keyword, LocalDate date, Long userId, Pageable pageable) {
        return campaignService.getPersonalizedCampaignsByKeyword(keyword, date, userId, pageable);
    }

    /**
     * 맞춤형 알림 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteKeywordAlert(Long userId, List<Long> alertIds) {
        List<KeywordCampaignAlert> alerts = keywordAlertRepository.findAllById(alertIds);

        // 모든 알림이 해당 사용자의 것인지 확인
        for (KeywordCampaignAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new UserException(ErrorMessage.USER_KEYWORD_ACCESS_DENIED);
            }
            alert.hide();
        }

        keywordAlertRepository.saveAll(alerts);
        log.info("키워드 알림 숨김 처리 완료: userId={}, count={}", userId, alertIds.size());
    }

    /**
     * 키워드 알림 읽음 처리 (배열)
     */
    @Transactional
    public void markKeywordAlertsAsRead(Long userId, List<Long> alertIds) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        List<KeywordCampaignAlert> alerts = keywordAlertRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (KeywordCampaignAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new UserException(ErrorMessage.USER_KEYWORD_ACCESS_DENIED);
            }
        }
        
        // 읽음 처리
        alerts.forEach(KeywordCampaignAlert::markAsRead);
        keywordAlertRepository.saveAll(alerts);
        
        log.info("키워드 알림 일괄 읽음 처리 완료: userId={}, count={}", userId, alertIds.size());
    }
} 