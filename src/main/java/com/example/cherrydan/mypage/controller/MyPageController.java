package com.example.cherrydan.mypage.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.inquiry.dto.InquiryRequestDTO;
import com.example.cherrydan.inquiry.dto.InquiryResponseDTO;
import com.example.cherrydan.inquiry.service.InquiryService;
import com.example.cherrydan.notice.dto.NoticeResponseDTO;
import com.example.cherrydan.notice.service.NoticeService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.version.dto.AppVersionResponseDTO;
import com.example.cherrydan.version.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final AppVersionService appVersionService;
    private final NoticeService noticeService;
    private final InquiryService inquiryService;

    @Operation(summary = "앱 버전 정보 조회")
    @GetMapping("/version")
    public ResponseEntity<ApiResponse<AppVersionResponseDTO>> getAppVersion() {
        AppVersionResponseDTO response = appVersionService.getLatestVersion();
        return ResponseEntity.ok(ApiResponse.success("앱 버전 정보 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "체리단 소식 목록 조회")
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<NoticeResponseDTO>>> getNotices(Pageable pageable) {
        Page<NoticeResponseDTO> response = noticeService.getActiveNotices(pageable);
        return ResponseEntity.ok(ApiResponse.success("체리단 소식 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 정보를 조회하고 조회수를 증가시킵니다.")
    @GetMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> getNoticeDetail(@PathVariable Long id) {
        NoticeResponseDTO response = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(ApiResponse.success("공지사항 상세 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "내 문의 목록 조회")
    @GetMapping("/inquiries")
    public ResponseEntity<ApiResponse<Page<InquiryResponseDTO>>> getMyInquiries(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable) {
        Page<InquiryResponseDTO> response = inquiryService.getUserInquiries(currentUser.getId(), pageable);
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
