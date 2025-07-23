package com.example.cherrydan.notice.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.notice.dto.NoticeResponseDTO;
import com.example.cherrydan.notice.service.NoticeService;
import com.example.cherrydan.notice.service.NoticeBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.cherrydan.notice.dto.NoticeBannerResponseDTO;

@RestController
@RequestMapping("/api/noticeboard")
@RequiredArgsConstructor
@Tag(name = "NoticeBoard", description = "공지사항 게시판 관련 API")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeBannerService noticeBannerService;

    @Operation(
        summary = "공지사항 목록 조회",
        description = """
            활성 상태인 공지사항 목록을 조회합니다.
            
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
    @GetMapping
    public ResponseEntity<ApiResponse<PageListResponseDTO<NoticeResponseDTO>>> getNotices(
            @Parameter(description = "페이지네이션 정보") 
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable
    ) {
        Page<NoticeResponseDTO> notices = noticeService.getActiveNotices(pageable);
        PageListResponseDTO<NoticeResponseDTO> response = PageListResponseDTO.from(notices);
        return ResponseEntity.ok(ApiResponse.success("공지사항 목록 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> getNoticeDetail(@PathVariable("id") Long id) {
        NoticeResponseDTO notice = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(ApiResponse.success("공지사항 상세 조회가 완료되었습니다.", notice));
    }

    @Operation(summary = "공감 버튼 클릭")
    @PostMapping("/{id}/empathy")
    public ResponseEntity<ApiResponse<NoticeResponseDTO>> toggleEmpathy(
            @PathVariable("id") Long id,
            @RequestParam("isEmpathy") boolean isEmpathy) {
        NoticeResponseDTO notice = noticeService.toggleEmpathy(id, isEmpathy);
        String message = isEmpathy ? "공감이 증가되었습니다." : "공감이 감소되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, notice));
    }

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<NoticeBannerResponseDTO>>> getBanners() {
        List<NoticeBannerResponseDTO> banners = noticeBannerService.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success("배너 목록 조회가 완료되었습니다.", banners));
    }
}