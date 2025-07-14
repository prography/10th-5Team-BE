package com.example.cherrydan.user.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.user.service.GuestModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest-mode")
@RequiredArgsConstructor
@Tag(name = "Guest Mode", description = "게스트모드 관련 API")
public class GuestModeController {

    private final GuestModeService guestModeService;

    @Operation(
        summary = "게스트모드 활성화 여부 조회",
        description = "iOS 앱에서 게스트모드 활성화 여부를 확인하는 API입니다."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getGuestModeStatus() {
        boolean isEnabled = guestModeService.isGuestModeEnabled();
        return ResponseEntity.ok(ApiResponse.success("게스트모드 상태 조회가 완료되었습니다.", isEnabled));
    }
} 