package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import java.util.List;

public interface BookmarkService {
    void addBookmark(Long userId, Long campaignId);
    void cancelBookmark(Long userId, Long campaignId);
    List<BookmarkResponseDTO> getBookmarks(Long userId);
    void deleteBookmark(Long userId, Long campaignId);
} 