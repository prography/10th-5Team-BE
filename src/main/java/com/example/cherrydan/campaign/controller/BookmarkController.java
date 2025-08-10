package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.service.BookmarkService;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.common.response.EmptyResponse;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.cherrydan.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.BookmarkSplitResponseDTO;

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

    @Operation(summary = "북마크 취소", description = "캠페인 북마크(찜)를 취소합니다. (is_active=0)")
    @PatchMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<EmptyResponse>> cancelBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.cancelBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success("북마크 취소 성공"));
    }

    @Operation(
        summary = "오늘+기간 남은 북마크 목록 조회",
        description = "오늘 이후 reviewerAnnouncement가 남아있는 북마크 목록을 조회합니다."
    )
    @GetMapping("/bookmarks/open")
    public ResponseEntity<ApiResponse<PageListResponseDTO<BookmarkResponseDTO>>> getOpenBookmarks(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable
    ) {
        PageListResponseDTO<BookmarkResponseDTO> result = bookmarkService.getOpenBookmarks(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("기간 남은 북마크 목록 조회 성공", result));
    }

    @Operation(
        summary = "기간 지난 북마크 목록 조회",
        description = "오늘 이전 reviewerAnnouncement가 지난 북마크 목록을 조회합니다."
    )
    @GetMapping("/bookmarks/closed")
    public ResponseEntity<ApiResponse<PageListResponseDTO<BookmarkResponseDTO>>> getClosedBookmarks(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable
    ) {
        PageListResponseDTO<BookmarkResponseDTO> result = bookmarkService.getClosedBookmarks(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("기간 지난 북마크 목록 조회 성공", result));
    }

    @Operation(summary = "북마크 완전 삭제", description = "캠페인 북마크(찜) 정보를 완전히 삭제합니다.")
    @DeleteMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.deleteBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success("북마크 삭제 성공"));
    }
} 