package com.example.cherrydan.activity.controller;

import com.example.cherrydan.activity.dto.ActivityAlertResponseDTO;
import com.example.cherrydan.activity.service.ActivityAlertService;
import com.example.cherrydan.user.dto.AlertIdsRequestDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Activity", description = "활동 페이지 관련 API")
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityAlertService activityAlertService;
    
    // === 기존 CampaignStatus 기반 활동 알림 API 제거됨 ===
    // 새로운 북마크 기반 ActivityAlert 시스템 사용

    // === 새로운 북마크 기반 활동 알림 API ===

    @Operation(
        summary = "북마크 기반 활동 알림 목록 조회",
        description = """
            사용자의 북마크 기반 활동 알림 목록을 조회합니다.
            북마크한 캠페인의 신청 마감이 3일 남았을 때 생성되는 알림들입니다.

            **쿼리 파라미터 예시:**
            - ?page=0&size=20&sort=alertDate,desc
            - ?page=1&size=10&sort=alertDate,asc

            **정렬 가능한 필드:**
            - alertDate: 알림 생성 날짜 (기본값, DESC)

            **주의:** 이는 Request Body가 아닌 **Query Parameter**입니다.
            """
    )
    @GetMapping("/bookmark-alerts")
    public ResponseEntity<ApiResponse<PageListResponseDTO<ActivityAlertResponseDTO>>> getBookmarkActivityAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PageableDefault(size = 20, sort = "alertDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ActivityAlertResponseDTO> alerts = activityAlertService.getUserActivityAlerts(currentUser.getId(), pageable);
        PageListResponseDTO<ActivityAlertResponseDTO> response = PageListResponseDTO.from(alerts);
        return ResponseEntity.ok(ApiResponse.success("북마크 활동 알림 목록 조회 성공", response));
    }

    @Operation(
        summary = "북마크 기반 활동 알림 개수 조회",
        description = "사용자의 북마크 기반 활동 알림 개수를 조회합니다."
    )
    @GetMapping("/bookmark-alerts/count")
    public ResponseEntity<ApiResponse<Long>> getBookmarkActivityAlertsCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Long alertsCount = activityAlertService.getUserActivityAlertsCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("북마크 활동 알림 개수 조회 성공", alertsCount));
    }

    @Operation(
        summary = "북마크 기반 활동 알림 삭제",
        description = "선택한 북마크 기반 활동 알림들을 삭제합니다. 본인의 알림이 아닌 경우 403 에러를 반환합니다."
    )
    @DeleteMapping("/bookmark-alerts")
    public ResponseEntity<ApiResponse<Void>> deleteBookmarkActivityAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody AlertIdsRequestDTO request
    ) {
        activityAlertService.deleteActivityAlert(currentUser.getId(), request.getAlertIds());
        return ResponseEntity.ok(ApiResponse.success("북마크 활동 알림 삭제 성공", null));
    }

    @Operation(
        summary = "북마크 기반 활동 알림 읽음 처리",
        description = "선택한 북마크 기반 활동 알림들을 읽음 상태로 변경합니다. 본인의 알림이 아닌 경우 403 에러를 반환합니다."
    )
    @PatchMapping("/bookmark-alerts/read")
    public ResponseEntity<ApiResponse<Void>> markBookmarkActivityAlertsAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody AlertIdsRequestDTO request
    ) {
        activityAlertService.markActivityAlertsAsRead(currentUser.getId(), request.getAlertIds());
        return ResponseEntity.ok(ApiResponse.success("북마크 활동 알림 읽음 처리 성공", null));
    }
} 