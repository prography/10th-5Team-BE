package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {
    void addBookmark(Long userId, Long campaignId);
    void cancelBookmark(Long userId, Long campaignId);
    Page<BookmarkResponseDTO> getBookmarks(Long userId, Pageable pageable);
    void deleteBookmark(Long userId, Long campaignId);
} 