package com.example.cherrydan.push.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.PushException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.push.dto.PushSettingsRequestDTO;
import com.example.cherrydan.push.dto.PushSettingsResponseDTO;
import com.example.cherrydan.push.dto.PushCategory;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserPushSettings;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PushSettingsService {
    private final UserRepository userRepository;

    /**
     * 사용자 푸시 알림 설정 조회
     */
    public PushSettingsResponseDTO getUserPushSettings(Long userId) {
        log.info("푸시 알림 설정 조회 시작 - userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserPushSettings settings = user.getPushSettings();
        if (settings == null) {
            log.info("기본 푸시 알림 설정 생성 - userId: {}", userId);
            settings = createDefaultPushSettings(user);
        }

        log.info("푸시 알림 설정 조회 완료 - userId: {}, pushEnabled: {}", userId, settings.getPushEnabled());
        return PushSettingsResponseDTO.from(settings);
    }

    /**
     * 사용자 푸시 알림 설정 업데이트
     */
    public PushSettingsResponseDTO updatePushSettings(Long userId, PushSettingsRequestDTO request) {
        log.info("푸시 알림 설정 업데이트 시작 - userId: {}, request: {}", userId, request);
        
        if (request == null) {
            throw new PushException(ErrorMessage.PUSH_SETTINGS_INVALID_REQUEST);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserPushSettings settings = user.getPushSettings();
        if (settings == null) {
            log.info("기본 푸시 알림 설정 생성 후 업데이트 - userId: {}", userId);
            settings = createDefaultPushSettings(user);
        }

        try {
            settings.updateSettings(
                    request.getActivityEnabled(),
                    request.getPersonalizedEnabled(),
                    request.getServiceEnabled(),
                    request.getMarketingEnabled(),
                    request.getPushEnabled()
            );

            log.info("푸시 알림 설정 업데이트 완료 - userId: {}, pushEnabled: {}", userId, settings.getPushEnabled());
            return PushSettingsResponseDTO.from(settings);
        } catch (Exception e) {
            log.error("푸시 알림 설정 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new PushException(ErrorMessage.PUSH_SETTINGS_UPDATE_FAILED);
        }
    }

    /**
     * 전체 푸시 알림 on/off
     */
    public PushSettingsResponseDTO togglePushEnabled(Long userId, boolean enabled) {
        log.info("전체 푸시 알림 토글 시작 - userId: {}, enabled: {}", userId, enabled);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserPushSettings settings = user.getPushSettings();
        if (settings == null) {
            log.info("기본 푸시 알림 설정 생성 후 토글 - userId: {}", userId);
            settings = createDefaultPushSettings(user);
        }

        try {
            // pushEnabled는 마스터 스위치 역할 (카테고리 설정은 보존)
            settings.setPushEnabled(enabled);
            log.info("전체 푸시 알림 토글 완료 - userId: {}, enabled: {} (카테고리 설정 보존)", userId, enabled);
            return PushSettingsResponseDTO.from(settings);
        } catch (Exception e) {
            log.error("전체 푸시 알림 토글 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new PushException(ErrorMessage.PUSH_SETTINGS_UPDATE_FAILED);
        }
    }

    /**
     * 특정 카테고리 푸시 알림 허용 여부 확인
     */
    public boolean isPushAllowed(Long userId, PushCategory category) {
        log.info("푸시 알림 허용 여부 확인 - userId: {}, category: {}", userId, category);
        
        if (category == null) {
            log.warn("잘못된 푸시 카테고리 - userId: {}", userId);
            throw new PushException(ErrorMessage.PUSH_CATEGORY_INVALID);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserPushSettings settings = user.getPushSettings();
        if (settings == null) {
            log.info("기본 푸시 알림 설정 생성 후 확인 - userId: {}", userId);
            settings = createDefaultPushSettings(user);
        }

        // 전체 푸시가 꺼져있으면 모든 알림 차단
        if (!settings.getPushEnabled()) {
            log.info("전체 푸시 알림 비활성화 - userId: {}", userId);
            return false;
        }

        // 전체 푸시가 켜져있을 때만 세부 설정 확인
        boolean isAllowed = switch (category) {
            case ACTIVITY -> settings.getActivityEnabled();
            case PERSONALIZED -> settings.getPersonalizedEnabled();
            case SERVICE -> settings.getServiceEnabled();
            case MARKETING -> settings.getMarketingEnabled();
        };
        
        log.info("푸시 알림 허용 여부 확인 완료 - userId: {}, category: {}, allowed: {}", userId, category, isAllowed);
        return isAllowed;
    }

    /**
     * 기본 푸시 설정 생성
     */
    public UserPushSettings createDefaultPushSettings(User user) {
        log.info("기본 푸시 알림 설정 생성 - userId: {}", user.getId());
        
        try {
            UserPushSettings defaultSettings = UserPushSettings.builder()
                    .activityEnabled(true)
                    .personalizedEnabled(true)
                    .serviceEnabled(true)
                    .marketingEnabled(true)
                    .pushEnabled(true)
                    .build();

            user.setPushSettings(defaultSettings);

            log.info("기본 푸시 알림 설정 생성 완료 - userId: {}", user.getId());
            return defaultSettings;
        } catch (Exception e) {
            log.error("기본 푸시 알림 설정 생성 실패 - userId: {}, error: {}", user.getId(), e.getMessage());
            throw new PushException(ErrorMessage.PUSH_SETTINGS_CREATE_FAILED);
        }
    }
}
