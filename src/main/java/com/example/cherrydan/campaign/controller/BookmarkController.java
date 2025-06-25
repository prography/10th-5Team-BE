package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.service.BookmarkService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{campaignId}/bookmark")
    public void addBookmark(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        System.out.println("currentUser.getId() = " + currentUser.getId());
        bookmarkService.addBookmark(currentUser.getId(), campaignId);
    }

    @PatchMapping("/{campaignId}/bookmark")
    public void cancelBookmark(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        System.out.println("currentUser.getId() = " + currentUser.getId());
        bookmarkService.cancelBookmark(currentUser.getId(), campaignId);
    }

    @GetMapping("/bookmarks")
    public List<BookmarkResponseDTO> getBookmarks(
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        System.out.println("currentUser.getId() = " + currentUser.getId());
        return bookmarkService.getBookmarks(currentUser.getId());
    }

    @DeleteMapping("/{campaignId}/bookmark")
    public void deleteBookmark(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        System.out.println("currentUser.getId() = " + currentUser.getId());
        bookmarkService.deleteBookmark(currentUser.getId(), campaignId);
    }
} 