package com.example.cherrydan.user.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.domain.KeywordCampaignAlert;
import com.example.cherrydan.user.repository.UserKeywordRepository;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.fcm.service.NotificationService;
import com.example.cherrydan.fcm.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import org.springframework.data.domain.Page;
import com.example.cherrydan.user.dto.UserKeywordResponseDTO;
import com.example.cherrydan.user.dto.KeywordCampaignAlertResponseDTO;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKeywordService {
    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;
    private final KeywordCampaignAlertRepository keywordAlertRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;

    @Transactional
    public void addKeyword(Long userId, String keyword) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (userKeywordRepository.findByUserIdAndKeyword(userId, keyword).isPresent()) {
            throw new IllegalStateException("이미 등록된 키워드입니다.");
        }
        userKeywordRepository.save(UserKeyword.builder().user(user).keyword(keyword).build());
    }

    @Transactional(readOnly = true)
    public Page<UserKeyword> getKeywords(Long userId, Pageable pageable) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userKeywordRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void removeKeyword(Long userId, String keyword) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userKeywordRepository.deleteByUserIdAndKeyword(userId, keyword);
    }

    @Transactional
    public void removeKeywordById(Long userId, Long keywordId) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 키워드가 해당 사용자의 것인지 확인 후 삭제
        UserKeyword userKeyword = userKeywordRepository.findByIdAndUserId(keywordId, userId)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없거나 권한이 없습니다."));
        
        userKeywordRepository.delete(userKeyword);
    }

    /**
     * 키워드 맞춤 캠페인 알림 대상 업데이트 (10개와 100개를 넘는 순간에만)
     * 새벽 5시에 배치 처리로 실행
     */
    @Transactional
    public void updateKeywordCampaignAlerts() {
        log.info("키워드 맞춤 캠페인 알림 배치 업데이트 시작");
        
        // 키워드별로 그룹핑해서 효율적으로 처리
        Map<String, List<UserKeyword>> keywordGroups = userKeywordRepository.findAllWithUser()
                .stream()
                .collect(Collectors.groupingBy(UserKeyword::getKeyword));
        
        LocalDate today = LocalDate.now();
        List<KeywordCampaignAlert> alertsToSave = new ArrayList<>();
        
        // 키워드별로 처리
        for (Map.Entry<String, List<UserKeyword>> entry : keywordGroups.entrySet()) {
            String keyword = entry.getKey();
            List<UserKeyword> userKeywords = entry.getValue();
            
            // 해당 키워드의 캠페인 수를 한 번만 조회
            long campaignCount = getCampaignCountByKeyword(keyword);
            
            // 해당 키워드를 등록한 모든 사용자에게 알림 처리
            for (UserKeyword userKeyword : userKeywords) {
                KeywordCampaignAlert alert = processKeywordAlert(userKeyword, keyword, (int) campaignCount, today);
                if (alert != null) {
                    alertsToSave.add(alert);
                    log.info("키워드 알림 생성: 사용자={}, 키워드={}, 캠페인수={}, 단계={}", 
                            userKeyword.getUser().getId(), keyword, campaignCount, 
                            campaignCount >= 100 ? "100개" : "10개");
                }
            }
        }
        
        // 벌크 저장으로 성능 최적화
        if (!alertsToSave.isEmpty()) {
            keywordAlertRepository.saveAll(alertsToSave);
            log.info("벌크 저장 완료: {}개 알림", alertsToSave.size());
        }
        
        log.info("키워드 맞춤 캠페인 알림 배치 업데이트 완료: 총 {}개 알림 생성", alertsToSave.size());
    }

    /**
     * 개별 사용자-키워드 조합에 대한 알림 처리
     * 사용자의 푸시 설정을 확인하여 조건에 맞는 경우에만 알림 생성
     */
    private KeywordCampaignAlert processKeywordAlert(UserKeyword userKeyword, String keyword, int campaignCount, LocalDate today) {
        // 1. 사용자 푸시 설정 확인
        User user = userKeyword.getUser();
        if (user.getPushSettings() == null || 
            !user.getPushSettings().getPushEnabled() || 
            !user.getPushSettings().getPersonalizedEnabled()) {
            log.info("사용자 푸시 설정으로 인해 키워드 알림 생성 제외: 사용자={}, 키워드={}", 
                    user.getId(), keyword);
            return null;
        }
        
        // 2. 알림 생성 조건 확인
        if (!shouldCreateKeywordAlert(userKeyword, keyword, campaignCount, today)) {
            return null;
        }
        
        // 3. 알림 엔티티 생성
        return createKeywordAlertEntity(userKeyword, keyword, campaignCount, today);
    }

    /**
     * 키워드로 매칭되는 활성 캠페인 수 조회
     */
    private long getCampaignCountByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0;
        }
        
        String trimmedKeyword = keyword.trim();
        
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            
            String likeKeyword = "%" + trimmedKeyword + "%";
            
            predicates.add(cb.or(
                    cb.like(root.get("title"), likeKeyword),
                    cb.like(root.get("address"), likeKeyword),
                    cb.like(root.get("benefit"), likeKeyword)
            ));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return campaignRepository.count(spec);
    }

    /**
     * 키워드 알림 생성 여부 확인 (10개 또는 100개를 넘는 순간에만)
     */
    private boolean shouldCreateKeywordAlert(UserKeyword userKeyword, String keyword, int campaignCount, LocalDate today) {
        // 기존 알림 조회
        KeywordCampaignAlert existingAlert = keywordAlertRepository
                .findByUserIdAndKeywordAndIsVisibleToUserTrue(userKeyword.getUser().getId(), keyword);
        
        if (existingAlert == null) {
            // 첫 번째 알림
            return campaignCount >= 10;
        }
        
        // 기존 알림 단계 확인
        int currentStage = existingAlert.getAlertStage();
        
        if (campaignCount >= 100 && currentStage < 2) {
            return true; // 100개 알림
        } else if (campaignCount >= 10 && campaignCount < 100 && currentStage < 1) {
            return true; // 10개 알림
        }
        
        return false;
    }
    
    /**
     * 키워드 알림 엔티티 생성 (벌크 저장용)
     */
    private KeywordCampaignAlert createKeywordAlertEntity(UserKeyword userKeyword, String keyword, int campaignCount, LocalDate today) {
        // 기존 알림 조회
        KeywordCampaignAlert existingAlert = keywordAlertRepository
                .findByUserIdAndKeywordAndIsVisibleToUserTrue(userKeyword.getUser().getId(), keyword);
        
        if (existingAlert != null) {
            // 기존 알림 업데이트
            existingAlert.updateAlertInfo(campaignCount, today);
            return existingAlert;
        } else {
            // 새 알림 생성
            int alertStage = campaignCount >= 100 ? 2 : 1;
            return KeywordCampaignAlert.builder()
                    .user(userKeyword.getUser())
                    .keyword(keyword)
                    .campaignCount(campaignCount)
                    .alertDate(today)
                    .alertStage(alertStage)
                    .isNotified(false)
                    .isVisibleToUser(true)
                    .build();
        }
    }

    /**
     * 키워드 맞춤 알림 발송 (범위별 배치 발송으로 최적화)
     */
    @Transactional
    public void sendKeywordCampaignNotifications() {
        log.info("키워드 맞춤 알림 발송 시작");
        
        List<KeywordCampaignAlert> unnotifiedAlerts = keywordAlertRepository.findUnnotifiedAlerts();
        
        if (unnotifiedAlerts.isEmpty()) {
            log.info("발송할 키워드 알림이 없습니다.");
            return;
        }
        
        // 사용자별로 그룹핑
        Map<Long, List<KeywordCampaignAlert>> groupedByUser = unnotifiedAlerts.stream()
                .collect(Collectors.groupingBy(alert -> alert.getUser().getId()));
        
        // 캠페인 수 범위별로 사용자 그룹핑
        Map<String, List<Long>> rangeGroups = new HashMap<>();
        rangeGroups.put("10+", new ArrayList<>());
        rangeGroups.put("100+", new ArrayList<>());
        
        List<KeywordCampaignAlert> allAlertsToUpdate = new ArrayList<>();
        
        // 각 사용자의 최대 캠페인 수를 기준으로 범위별 그룹 분류
        for (Map.Entry<Long, List<KeywordCampaignAlert>> entry : groupedByUser.entrySet()) {
            Long userId = entry.getKey();
            List<KeywordCampaignAlert> userAlerts = entry.getValue();
            
            try {
                // 해당 사용자의 모든 알림 중 최대 캠페인 수 찾기
                int maxCampaignCount = userAlerts.stream()
                        .mapToInt(KeywordCampaignAlert::getCampaignCount)
                        .max()
                        .orElse(0);
                
                // 최대값 기준으로 그룹 분류 (더 높은 단계 우선)
                if (maxCampaignCount >= 100) {
                    rangeGroups.get("100+").add(userId);
                } else {
                    rangeGroups.get("10+").add(userId);
                }
                
                // 발송 완료 후 업데이트할 알림들 준비
                allAlertsToUpdate.addAll(userAlerts);
                
                log.info("키워드 알림 발송 대상 추가: 사용자={}, 키워드수={}, 최대캠페인수={}, 그룹={}", 
                        userId, userAlerts.size(), maxCampaignCount, 
                        maxCampaignCount >= 100 ? "100+" : "10+");
                
            } catch (Exception e) {
                log.error("키워드 알림 발송 대상 처리 실패: 사용자={}, 오류={}", userId, e.getMessage());
            }
        }
        
        int totalSentUsers = 0;
        
        // 범위별로 배치 발송 (최대 2번의 API 호출)
        for (Map.Entry<String, List<Long>> rangeEntry : rangeGroups.entrySet()) {
            String range = rangeEntry.getKey();
            List<Long> userIds = rangeEntry.getValue();
            
            if (userIds.isEmpty()) {
                continue;
            }
            
            try {
                String title = getRangeAlertTitle(range);
                String body = getRangeAlertBody(range);
                
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .title(title)
                        .body(body)
                        .data(java.util.Map.of(
                                "type", "keyword_campaign",
                                "range", range,
                                "action", "open_personalized_page"
                        ))
                        .priority("high")
                        .build();
                
                // 범위별 배치 발송
                notificationService.sendNotificationToUsers(userIds, notificationRequest);
                totalSentUsers += userIds.size();
                
                log.info("키워드 알림 범위별 배치 발송 완료: 범위={}, 사용자수={}", range, userIds.size());
                
            } catch (Exception e) {
                log.error("키워드 알림 범위별 배치 발송 실패: 범위={}, 오류={}", range, e.getMessage());
                // 해당 범위 발송 실패 시에도 다른 범위는 계속 처리
            }
        }
        
        // 발송 성공 시 알림 상태 업데이트
        if (totalSentUsers > 0) {
            try {
                allAlertsToUpdate.forEach(KeywordCampaignAlert::markAsNotified);
                keywordAlertRepository.saveAll(allAlertsToUpdate);
                log.info("알림 상태 벌크 업데이트 완료: {}개", allAlertsToUpdate.size());
            } catch (Exception e) {
                log.error("알림 상태 업데이트 실패: {}", e.getMessage());
            }
        }
        
        log.info("키워드 맞춤 알림 발송 완료: 총 {}명에게 발송 (10+범위: {}명, 100+범위: {}명)", 
                totalSentUsers, rangeGroups.get("10+").size(), rangeGroups.get("100+").size());
    }

    /**
     * 범위별 알림 제목 생성
     */
    private String getRangeAlertTitle(String range) {
        if ("100+".equals(range)) {
            return "키워드 맞춤 캠페인 100+";
        } else {
            return "키워드 맞춤 캠페인 10+";
        }
    }

    /**
     * 범위별 알림 내용 생성
     */
    private String getRangeAlertBody(String range) {
        if ("100+".equals(range)) {
            return "등록하신 키워드로 100개 이상의 캠페인이 매칭되었습니다! 놓치지 마세요";
        } else {
            return "등록하신 키워드로 10개 이상의 캠페인이 매칭되었습니다. 지금 확인하세요";
        }
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
    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> getPersonalizedCampaignsByKeyword(Long userId, String keyword, Pageable pageable) {
        // 해당 유저가 등록한 키워드인지 검증
        if (!userKeywordRepository.existsByUserIdAndKeyword(userId, keyword)) {
            throw new IllegalArgumentException("등록되지 않은 키워드입니다.");
        }

        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            String likeKeyword = "%" + keyword.trim() + "%";
            predicates.add(cb.or(
                cb.like(root.get("title"), likeKeyword),
                cb.like(root.get("address"), likeKeyword),
                cb.like(root.get("benefit"), likeKeyword)
            ));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
        return campaigns.map(CampaignResponseDTO::fromEntity);
    }

    /**
     * 맞춤형 알림 삭제 (배열)
     */
    @Transactional
    public void deleteKeywordAlert(Long userId, List<Long> alertIds) {
        List<KeywordCampaignAlert> alerts = keywordAlertRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (KeywordCampaignAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new SecurityException("본인 알림만 삭제할 수 있습니다.");
            }
        }
        
        keywordAlertRepository.deleteAll(alerts);
        log.info("키워드 알림 삭제 완료: userId={}, count={}", userId, alertIds.size());
    }

    /**
     * 키워드 알림 읽음 처리 (배열)
     */
    @Transactional
    public void markKeywordAlertsAsRead(Long userId, List<Long> alertIds) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<KeywordCampaignAlert> alerts = keywordAlertRepository.findAllById(alertIds);
        
        // 모든 알림이 해당 사용자의 것인지 확인
        for (KeywordCampaignAlert alert : alerts) {
            if (!alert.getUser().getId().equals(userId)) {
                throw new SecurityException("본인의 알림만 읽음 처리할 수 있습니다.");
            }
        }
        
        // 읽음 처리
        alerts.forEach(KeywordCampaignAlert::markAsRead);
        keywordAlertRepository.saveAll(alerts);
        
        log.info("키워드 알림 일괄 읽음 처리 완료: userId={}, count={}", userId, alertIds.size());
    }
} 