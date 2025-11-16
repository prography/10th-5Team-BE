package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.BookmarkDeleteDTO;
import com.example.cherrydan.campaign.dto.BookmarkCancelDTO;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.domain.BookmarkCase;
import com.example.cherrydan.campaign.service.BookmarkService;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.common.response.EmptyResponse;
import com.example.cherrydan.common.exception.CampaignException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Bookmark", description = "캠페인 북마크(찜) 관련 API")
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "캠페인에 북마크(찜)를 추가합니다.")
    @PostMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<EmptyResponse>> addBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.addBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success("북마크 추가 성공"));
    }

    @Operation(summary = "북마크 취소", description = "여러 캠페인 북마크(찜)를 한 번에 취소합니다. (is_active=0)")
    @PatchMapping("/bookmark")
    public ResponseEntity<ApiResponse<EmptyResponse>> cancelBookmark(
            @Parameter(description = "북마크 취소 요청", required = true) @Valid @RequestBody BookmarkCancelDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.cancelBookmarks(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("북마크 취소 성공"));
    }

    @Operation(
        summary = "북마크 목록 조회",
        description = "case 파라미터(open/closed)로 북마크 목록을 조회합니다. open: 기간 남은 북마크, closed: 기간 지난 북마크"
    )
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<PageListResponseDTO<BookmarkResponseDTO>>> getBookmarksByCase(
            @Parameter(description = "북마크 케이스 (open: 기간 남은 북마크, closed: 기간 지난 북마크)", required = false)
            @RequestParam(value = "case", defaultValue = "open") String caseParam,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable
    ) {
        BookmarkCase bookmarkCase;
        try {
            bookmarkCase = BookmarkCase.fromCode(caseParam.trim());
        } catch (IllegalArgumentException e) {
            throw new CampaignException(ErrorMessage.CAMPAIGN_STATUS_INVALID);
        }
        
        PageListResponseDTO<BookmarkResponseDTO> result = bookmarkService.getBookmarksByCase(currentUser.getId(), bookmarkCase, pageable);
        String message = bookmarkCase == BookmarkCase.LIKED_OPEN ? "신청 가능한 공고 목록 조회 성공" : "신청 마감된 공고 목록 조회 성공";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @Operation(summary = "북마크 완전 삭제", description = "캠페인 북마크(찜) 정보를 완전히 삭제합니다.")
    @DeleteMapping("/bookmark")
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteBookmark(
            @Parameter(description = "북마크 삭제 요청", required = true) @Valid @RequestBody BookmarkDeleteDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.deleteBookmark(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("북마크 삭제 성공"));
    }
} 