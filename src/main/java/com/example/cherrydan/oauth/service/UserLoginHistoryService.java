package com.example.cherrydan.oauth.service;

import com.example.cherrydan.user.domain.UserLoginHistory;
import com.example.cherrydan.user.repository.UserLoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 사용자 로그인 기록 관리 서비스
 * 로그인 기록 관리 책임을 분리하여 단일 책임 원칙 준수
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserLoginHistoryService {
    
    private final UserLoginHistoryRepository loginHistoryRepository;
    
    /**
     * 로그인 기록 저장
     * 로그인 기록 실패가 전체 로그인 프로세스에 영향을 주지 않도록 처리
     */
    public void recordLogin(Long userId) {
        try {
            UserLoginHistory loginHistory = createLoginHistory(userId);
            loginHistoryRepository.save(loginHistory);
            
            log.info("Login history recorded: userId={}, timestamp={}",
                userId, loginHistory.getLoginDate());
            
        } catch (Exception e) {
            // 로그인 기록 저장 실패는 로그인 프로세스를 중단시키지 않음
            log.error("Failed to record login history for userId={}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * 로그인 기록 엔티티 생성
     * 팩토리 메서드 패턴 적용
     */
    private UserLoginHistory createLoginHistory(Long userId) {
        return UserLoginHistory.builder()
                .userId(userId)
                .loginDate(LocalDateTime.now())
                .build();
    }
}