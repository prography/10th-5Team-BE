package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.service.BookmarkService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.cherrydan.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Bookmark", description = "캠페인 북마크(찜) 관련 API")
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "캠페인에 북마크(찜)를 추가합니다.")
    @PostMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.addBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "북마크 취소", description = "캠페인 북마크(찜)를 취소합니다. (is_active=0)")
    @PatchMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> cancelBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.cancelBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "내 북마크 목록 조회", description = "내가 북마크(찜)한 캠페인 목록을 조회합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<List<BookmarkResponseDTO>>> getBookmarks(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        List<BookmarkResponseDTO> result = bookmarkService.getBookmarks(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "북마크 완전 삭제", description = "캠페인 북마크(찜) 정보를 완전히 삭제합니다.")
    @DeleteMapping("/{campaignId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.deleteBookmark(currentUser.getId(), campaignId);
        return ResponseEntity.ok(ApiResponse.success());
    }
} 