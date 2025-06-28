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
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import com.example.cherrydan.user.dto.UserKeywordResponseDTO;
import com.example.cherrydan.user.dto.KeywordCampaignAlertResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKeywordService {
    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;
    private final KeywordCampaignAlertRepository keywordAlertRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;

    // 캐시: 키워드별 캠페인 수 (배치 처리 중에만 사용)
    private final Map<String, Long> keywordCampaignCountCache = new ConcurrentHashMap<>();

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
    public List<UserKeyword> getKeywords(Long userId) {
        // 활성 사용자인지 확인
        userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userKeywordRepository.findByUserId(userId);
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
        
        // 캐시 초기화
        keywordCampaignCountCache.clear();
        
        // 키워드별로 그룹핑해서 효율적으로 처리
        Map<String, List<UserKeyword>> keywordGroups = userKeywordRepository.findAllWithUser()
                .stream()
                .collect(Collectors.groupingBy(UserKeyword::getKeyword));
        
        LocalDate today = LocalDate.now();
        int totalAlerts = 0;
        List<KeywordCampaignAlert> alertsToSave = new ArrayList<>();
        
        for (Map.Entry<String, List<UserKeyword>> entry : keywordGroups.entrySet()) {
            String keyword = entry.getKey();
            List<UserKeyword> userKeywords = entry.getValue();
            
            // 키워드당 한 번만 캠페인 수 계산 (캐시 활용)
            long campaignCount = getCampaignCountByKeyword(keyword);
            
            for (UserKeyword userKeyword : userKeywords) {
                if (shouldCreateKeywordAlert(userKeyword, keyword, (int) campaignCount, today)) {
                    KeywordCampaignAlert alert = createKeywordAlertEntity(userKeyword, keyword, (int) campaignCount, today);
                    if (alert != null) {
                        alertsToSave.add(alert);
                        totalAlerts++;
                        log.debug("키워드 알림 생성: 사용자={}, 키워드={}, 캠페인수={}, 단계={}", 
                                userKeyword.getUser().getId(), keyword, campaignCount, 
                                campaignCount >= 100 ? "100개" : "10개");
                    }
                }
            }
        }
        
        // 벌크 저장으로 성능 최적화
        if (!alertsToSave.isEmpty()) {
            keywordAlertRepository.saveAll(alertsToSave);
            log.info("벌크 저장 완료: {}개 알림", alertsToSave.size());
        }
        
        // 캐시 정리
        keywordCampaignCountCache.clear();
        
        log.info("키워드 맞춤 캠페인 알림 배치 업데이트 완료: 총 {}개 알림 생성", totalAlerts);
    }

    /**
     * 키워드로 매칭되는 활성 캠페인 수 조회 (캐싱 적용)
     */
    private long getCampaignCountByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0;
        }
        
        // 캐시 확인
        return keywordCampaignCountCache.computeIfAbsent(keyword, this::searchCampaignCountByKeyword);
    }
    
    /**
     * 키워드로 매칭되는 활성 캠페인 수 조회 (JPA Specification)
     */
    private long searchCampaignCountByKeyword(String keyword) {
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
     * 키워드 맞춤 알림 발송
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
        
        int totalSent = 0;
        List<KeywordCampaignAlert> alertsToUpdate = new ArrayList<>();
        
        for (Map.Entry<Long, List<KeywordCampaignAlert>> entry : groupedByUser.entrySet()) {
            Long userId = entry.getKey();
            List<KeywordCampaignAlert> userAlerts = entry.getValue();
            
            try {
                // 가장 많은 캠페인 수를 가진 키워드로 대표 알림 생성
                KeywordCampaignAlert topAlert = userAlerts.stream()
                        .max((a1, a2) -> Integer.compare(a1.getCampaignCount(), a2.getCampaignCount()))
                        .orElse(userAlerts.get(0));
                
                String title = getKeywordAlertTitle(topAlert.getCampaignCount());
                String body = getKeywordAlertBody(topAlert.getKeyword(), topAlert.getCampaignCount());
                
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .title(title)
                        .body(body)
                        .data(java.util.Map.of(
                                "type", "keyword_campaign",
                                "keyword", topAlert.getKeyword(),
                                "campaign_count", String.valueOf(topAlert.getCampaignCount()),
                                "action", "open_personalized_page"
                        ))
                        .priority("high")
                        .build();
                
                notificationService.sendNotificationToUser(userId, notificationRequest);
                
                // 알림 발송 완료 표시 (벌크 업데이트용)
                userAlerts.forEach(alert -> {
                    alert.markAsNotified();
                    alertsToUpdate.add(alert);
                });
                
                totalSent++;
                log.debug("키워드 맞춤 알림 발송 완료: 사용자={}, 키워드수={}", userId, userAlerts.size());
                
            } catch (Exception e) {
                log.error("키워드 맞춤 알림 발송 실패: 사용자={}, 오류={}", userId, e.getMessage());
            }
        }
        
        // 벌크 업데이트로 성능 최적화
        if (!alertsToUpdate.isEmpty()) {
            keywordAlertRepository.saveAll(alertsToUpdate);
            log.info("알림 상태 벌크 업데이트 완료: {}개", alertsToUpdate.size());
        }
        
        log.info("키워드 맞춤 알림 발송 완료: 총 {}명에게 발송", totalSent);
    }


    /**
     * 사용자의 키워드 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<KeywordCampaignAlertResponseDTO> getUserKeywordAlerts(Long userId) {
        return keywordAlertRepository.findByUserIdAndIsVisibleToUserTrue(userId)
                .stream()
                .map(KeywordCampaignAlertResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private String getKeywordAlertTitle(int campaignCount) {
        if (campaignCount >= 100) {
            return "키워드 맞춤 캠페인 100+";
        } else {
            return "키워드 맞춤 캠페인 10+";
        }
    }

    private String getKeywordAlertBody(String keyword, int campaignCount) {
        if (campaignCount >= 100) {
            return String.format("'%s' 키워드로 %d개의 캠페인이 매칭되었습니다! 놓치지 마세요.", 
                    keyword, campaignCount);
        } else {
            return String.format("'%s' 키워드로 %d개의 캠페인이 매칭되었습니다.", 
                    keyword, campaignCount);
        }
    }

    /**
     * 특정 키워드로 맞춤형 캠페인 목록 조회
     */
    @Transactional(readOnly = true)
    public CampaignListResponseDTO getPersonalizedCampaignsByKeyword(Long userId, String keyword, Pageable pageable) {
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
        List<CampaignResponseDTO> content = campaigns.getContent().stream()
                .map(CampaignResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return CampaignListResponseDTO.builder()
                .content(content)
                .page(campaigns.getNumber())
                .size(campaigns.getSize())
                .totalElements(campaigns.getTotalElements())
                .totalPages(campaigns.getTotalPages())
                .hasNext(campaigns.hasNext())
                .hasPrevious(campaigns.hasPrevious())
                .build();
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