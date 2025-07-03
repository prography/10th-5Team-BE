package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.dto.BookmarkSplitResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookmarkService {
    void addBookmark(Long userId, Long campaignId);
    void cancelBookmark(Long userId, Long campaignId);
    BookmarkSplitResponseDTO getBookmarks(Long userId, Pageable pageable);
    void deleteBookmark(Long userId, Long campaignId);
} 