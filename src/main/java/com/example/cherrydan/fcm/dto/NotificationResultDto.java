package com.example.cherrydan.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 알림 전송 결과 응답 DTO
 * 알림 전송 결과를 클라이언트에게 전달하기 위한 공용 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResultDto {
    
    /**
     * 전송 성공 수
     */
    private int successCount;
    
    /**
     * 전송 실패 수
     */
    private int failureCount;
    
    /**
     * 총 전송 대상 수
     */
    private int totalCount;
    
    /**
     * 전송 결과 상세 메시지
     */
    private String details;
    
    /**
     * 전송 완료 시간
     */
    private LocalDateTime completedAt;
    
    /**
     * 성공한 사용자 ID 목록
     */
    private List<Long> successfulUserIds;
    
    /**
     * 추가 정보 (필요시 사용)
     */
    private Map<String, Object> additionalInfo;
    
    /**
     * 단일 사용자 전송 성공 결과 생성
     */
    public static NotificationResultDto singleSuccess() {
        return NotificationResultDto.builder()
                .successCount(1)
                .failureCount(0)
                .totalCount(1)
                .details("알림 전송 성공")
                .completedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 다중 사용자 전송 결과 생성
     */
    public static NotificationResultDto multipleResult(int successCount, int failureCount, String details) {
        return NotificationResultDto.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .totalCount(successCount + failureCount)
                .details(details)
                .completedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 다중 사용자 전송 결과 생성 (성공한 사용자 ID 포함)
     */
    public static NotificationResultDto multipleResult(int successCount, int failureCount, String details, List<Long> successfulUserIds) {
        return NotificationResultDto.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .totalCount(successCount + failureCount)
                .details(details)
                .completedAt(LocalDateTime.now())
                .successfulUserIds(successfulUserIds)
                .build();
    }
    
    /**
     * 토픽 전송 결과 생성
     */
    public static NotificationResultDto topicResult(String topicName) {
        return NotificationResultDto.builder()
                .successCount(1)
                .failureCount(0)
                .totalCount(1)
                .details(String.format("토픽 '%s' 알림 전송 완료", topicName))
                .completedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 전송 성공 여부 확인
     */
    public boolean isSuccess() {
        return successCount > 0;
    }
}
