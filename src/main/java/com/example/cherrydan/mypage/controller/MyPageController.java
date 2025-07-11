package com.example.cherrydan.mypage.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.inquiry.dto.InquiryRequestDTO;
import com.example.cherrydan.inquiry.dto.InquiryResponseDTO;
import com.example.cherrydan.inquiry.service.InquiryService;
import com.example.cherrydan.notice.dto.NoticeResponseDTO;
import com.example.cherrydan.notice.service.NoticeService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.push.dto.PushSettingsRequestDTO;
import com.example.cherrydan.push.dto.PushSettingsResponseDTO;
import com.example.cherrydan.push.service.PushSettingsService;
import com.example.cherrydan.user.dto.UserTosRequestDTO;
import com.example.cherrydan.user.dto.UserTosResponseDTO;
import com.example.cherrydan.user.service.UserTosService;
import com.example.cherrydan.version.dto.AppVersionResponseDTO;
import com.example.cherrydan.version.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final AppVersionService appVersionService;
    private final InquiryService inquiryService;
    private final PushSettingsService pushSettingsService;
    private final UserTosService userTosService;

    @Operation(summary = "이용약관 동의 설정 조회")
    @GetMapping("/tos")
    public ResponseEntity<ApiResponse<UserTosResponseDTO>> getUserTos(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserTosResponseDTO response = userTosService.getUserTos(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("이용약관 동의 설정 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "이용약관 동의 설정 업데이트")
    @PutMapping("/tos")
    public ResponseEntity<ApiResponse<UserTosResponseDTO>> updateUserTos(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody UserTosRequestDTO request) {
        UserTosResponseDTO response = userTosService.updateUserTos(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("이용약관 동의 설정이 업데이트되었습니다.", response));
    }

    @Operation(summary = "푸시 알림 설정 조회")
    @GetMapping("/push-settings")
    public ResponseEntity<ApiResponse<PushSettingsResponseDTO>> getPushSettings(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        PushSettingsResponseDTO response = pushSettingsService.getUserPushSettings(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("푸시 알림 설정 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "푸시 알림 설정 업데이트")
    @PutMapping("/push-settings")
    public ResponseEntity<ApiResponse<PushSettingsResponseDTO>> updatePushSettings(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody PushSettingsRequestDTO request) {
        PushSettingsResponseDTO response = pushSettingsService.updatePushSettings(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("푸시 알림 설정이 업데이트되었습니다.", response));
    }

    @Operation(summary = "푸시 알림 전체 on/off")
    @PatchMapping("/push-settings/toggle")
    public ResponseEntity<ApiResponse<PushSettingsResponseDTO>> togglePushEnabled(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("enabled") boolean enabled) {
        PushSettingsResponseDTO response = pushSettingsService.togglePushEnabled(currentUser.getId(), enabled);
        return ResponseEntity.ok(ApiResponse.success("푸시 알림 전체 설정이 변경되었습니다.", response));
    }

    @Operation(summary = "앱 버전 정보 조회")
    @GetMapping("/version")
    public ResponseEntity<ApiResponse<AppVersionResponseDTO>> getAppVersion() {
        AppVersionResponseDTO response = appVersionService.getLatestVersion();
        return ResponseEntity.ok(ApiResponse.success("앱 버전 정보 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "내 문의 상세 조회")
    @GetMapping("/inquiries/{id}")
    public ResponseEntity<ApiResponse<InquiryResponseDTO>> getMyInquiryDetail(@PathVariable("id") Long id) {
        InquiryResponseDTO response = inquiryService.getInquiryDetail(id);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 조회가 완료되었습니다.", response));
    }

    @Operation(
        summary = "내 문의 목록 조회",
        description = """
            사용자가 등록한 문의 목록을 조회합니다.
            
            **쿼리 파라미터 예시:**
            - ?page=0&size=20&sort=updatedAt,desc
            - ?page=1&size=10&sort=updatedAt,asc
            
            **정렬 가능한 필드:**
            - updatedAt: 수정 시각 (기본값, DESC)
            
            **여러 정렬 조건 (쿼리 파라미터):**
            - ?sort=updatedAt,desc&sort=id,asc (복수 정렬)
            - ?sort=updatedAt,desc (단일 정렬, 기본값)
            - ?sort=updatedAt,asc (오래된 순)
            
            **주의:** 이는 Request Body가 아닌 **Query Parameter**입니다.
            """
    )
    @GetMapping("/inquiries")
    public ResponseEntity<ApiResponse<PageListResponseDTO<InquiryResponseDTO>>> getMyInquiries(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(description = "페이지네이션 정보")
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable
    ) {
        Page<InquiryResponseDTO> inquiries = inquiryService.getUserInquiries(currentUser.getId(), pageable);
        PageListResponseDTO<InquiryResponseDTO> response = PageListResponseDTO.from(inquiries);
        return ResponseEntity.ok(ApiResponse.success("문의 목록 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "1:1 문의 등록")
    @PostMapping("/inquiries")
    public ResponseEntity<ApiResponse<InquiryResponseDTO>> createInquiry(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody InquiryRequestDTO request) {
        InquiryResponseDTO response = inquiryService.createInquiry(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("문의가 등록되었습니다.", response));
    }
}
