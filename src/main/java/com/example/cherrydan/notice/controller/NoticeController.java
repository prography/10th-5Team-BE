package com.example.cherrydan.notice.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.notice.dto.NoticeResponseDTO;
import com.example.cherrydan.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지사항 관련 API")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponseDTO>>> getNotices(Pageable pageable) {
        Page<NoticeResponseDTO> response = noticeService.getActiveNotices(pageable);
        return ResponseEntity.ok(ApiResponse.success("공지사항 목록 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> getNoticeDetail(@PathVariable Long id) {
        NoticeResponseDTO notice = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(ApiResponse.success("공지사항 상세 조회가 완료되었습니다.", notice));
    }

    @Operation(summary = "공감 버튼 클릭")
    @PostMapping("/{id}/empathy")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> incrementEmpathy(@PathVariable Long id) {
        NoticeResponseDTO notice = noticeService.incrementEmpathyCount(id);
        return ResponseEntity.ok(ApiResponse.success("공감이 반영되었습니다.", notice));
    }
} 