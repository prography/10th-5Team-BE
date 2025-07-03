package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.service.BookmarkService;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.PageResponse;
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

@Tag(name = "Bookmark", description = "캠페인 북마크(찜) 관련 API")
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "캠페인에 북마크(찜)를 추가합니다.")
    @PostMapping("/{campaignId}/bookmark")
    public void addBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.addBookmark(currentUser.getId(), campaignId);
    }

    @Operation(summary = "북마크 취소", description = "캠페인 북마크(찜)를 취소합니다. (is_active=0)")
    @PatchMapping("/{campaignId}/bookmark")
    public void cancelBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.cancelBookmark(currentUser.getId(), campaignId);
    }

    @Operation(
        summary = "내 북마크 목록 조회", 
        description = """
            내가 북마크(찜)한 캠페인 목록을 조회합니다.
            
            **Request Body 예시:**
            ```json
            {
              "page": 0,
              "size": 20
            }
            ```
            
            **정렬**: 북마크 생성 시각 내림차순 (고정)
            """
    )
    @GetMapping("/bookmarks")
    public ApiResponse<PageResponse<BookmarkResponseDTO>> getBookmarks(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<BookmarkResponseDTO> bookmarks = bookmarkService.getBookmarks(currentUser.getId(), pageable);
        PageResponse<BookmarkResponseDTO> response = PageResponse.from(bookmarks);
        return ApiResponse.success("북마크 목록 조회 성공", response);
    }

    @Operation(summary = "북마크 완전 삭제", description = "캠페인 북마크(찜) 정보를 완전히 삭제합니다.")
    @DeleteMapping("/{campaignId}/bookmark")
    public void deleteBookmark(
            @Parameter(description = "캠페인 ID", required = true) @PathVariable Long campaignId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        bookmarkService.deleteBookmark(currentUser.getId(), campaignId);
    }
} 