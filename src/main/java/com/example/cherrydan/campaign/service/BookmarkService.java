package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.BookmarkDeleteDTO;
import com.example.cherrydan.campaign.dto.BookmarkCancelDTO;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookmarkService {
    void addBookmark(Long userId, Long campaignId);
    void cancelBookmarks(Long userId, BookmarkCancelDTO request);
    PageListResponseDTO<BookmarkResponseDTO> getOpenBookmarks(Long userId, Pageable pageable);
    PageListResponseDTO<BookmarkResponseDTO> getClosedBookmarks(Long userId, Pageable pageable);
    void deleteBookmark(Long userId, BookmarkDeleteDTO request);
} 