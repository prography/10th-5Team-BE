package com.example.cherrydan.activity.controller;

import com.example.cherrydan.activity.dto.UnreadAlertCountResponseDTO;
import com.example.cherrydan.activity.service.AlertService;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Alert", description = "알림 관련 API")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(
        summary = "미읽은 알림 개수 조회",
        description = "사용자의 미읽은 알림 개수를 조회합니다. 활동 알림과 키워드 알림의 개수를 각각 제공하며, 전체 개수도 함께 반환합니다."
    )
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadAlertCountResponseDTO>> getUnreadAlertCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        UnreadAlertCountResponseDTO result = alertService.getUnreadAlertCount(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
