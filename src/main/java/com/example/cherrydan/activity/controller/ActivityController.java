package com.example.cherrydan.activity.controller;

import com.example.cherrydan.activity.dto.ActivityNotificationResponseDTO;
import com.example.cherrydan.activity.service.ActivityService;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Activity", description = "활동 페이지 관련 API")
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    @Operation(
        summary = "내 활동 알림 목록 조회",
        description = """
            사용자의 활동 알림 목록을 조회합니다. 캠페인 타입에 따라 알림 타입이 구분됩니다.
            
            **쿼리 파라미터 예시:**
            - ?page=0&size=20&sort=activityNotifiedAt,desc
            - ?page=1&size=10&sort=activityNotifiedAt,asc
            
            **정렬 가능한 필드:**
            - activityNotifiedAt: 활동 알림 생성 시각 (기본값, DESC)
            
            **여러 정렬 조건 (쿼리 파라미터):**
            - ?sort=activityNotifiedAt,desc&sort=id,asc (복수 정렬)
            - ?sort=activityNotifiedAt,desc (단일 정렬, 기본값)
            - ?sort=activityNotifiedAt,asc (오래된 순)
            
            **주의:** 이는 Request Body가 아닌 **Query Parameter**입니다.
            """
    )
    @GetMapping("/notifications")
    public ApiResponse<PageListResponseDTO<ActivityNotificationResponseDTO>> getActivityNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PageableDefault(size = 20, sort = "activityNotifiedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ActivityNotificationResponseDTO> notifications = activityService.getActivityNotifications(currentUser.getId(), pageable);
        PageListResponseDTO<ActivityNotificationResponseDTO> response = PageListResponseDTO.from(notifications);
        return ApiResponse.success("활동 알림 목록 조회 성공", response);
    }
    
    @Operation(
        summary = "활동 알림 읽음 처리",
        description = "활동 알림을 읽음 처리합니다. 1개 또는 여러개 모두 배열로 전달하세요."
    )
    @PutMapping("/notifications/read")
    public ApiResponse<Void> markNotificationsAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody List<Long> campaignStatusIds
    ) {
        activityService.markNotificationsAsRead(currentUser.getId(), campaignStatusIds);
        return ApiResponse.success("활동 알림 읽음 처리 완료", null);
    }
    
    @Operation(
        summary = "활동 알림 수동 발송 (개발/테스트용)",
        description = """
            활동 알림을 수동으로 발송합니다.
            
            **주의사항:**
            - 이 API는 개발 및 테스트 목적으로만 사용하세요
            - 실제 운영에서는 매일 오전 10시에 자동으로 실행됩니다
            - 불필요한 알림 발송을 방지하기 위해 신중하게 사용하세요
            
            **사용 시나리오:**
            - 알림 로직 개발/수정 후 즉시 테스트
            - 알림 발송 기능 디버깅
            - 긴급 상황 시 즉시 알림 발송
            
            **자동 실행 스케줄:**
            - 매일 오전 10:00 (Asia/Seoul)
            - 3일 이내 마감 캠페인 대상
            - 사용자별 푸시 설정 확인 후 발송
            """
    )
    @PostMapping("/notifications/send")
    public ApiResponse<Void> sendActivityNotifications() {
        activityService.sendActivityNotifications();
        return ApiResponse.success("활동 알림 발송 완료", null);
    }

    @Operation(
        summary = "활동 알림 삭제",
        description = "활동 알림을 삭제합니다. 1개 또는 여러개 모두 배열로 전달하세요."
    )
    @DeleteMapping("/alerts")
    public ApiResponse<Void> deleteActivityAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody List<Long> alertIds
    ) {
        activityService.deleteActivityAlerts(currentUser.getId(), alertIds);
        return ApiResponse.success("활동 알림 삭제 완료", null);
    }
} 