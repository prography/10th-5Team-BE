package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.dto.BookmarkSplitResponseDTO;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.data.domain.PageImpl;
import com.example.cherrydan.common.response.PageListResponseDTO;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public void addBookmark(Long userId, Long campaignId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        Optional<Bookmark> optionalBookmark = bookmarkRepository.findByUserAndCampaign(user, campaign);
        if (optionalBookmark.isPresent()) {
            Bookmark bookmark = optionalBookmark.get();
            bookmark.setIsActive(true);
            bookmarkRepository.save(bookmark);
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .campaign(campaign)
                    .isActive(true)
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    @Override
    @Transactional
    public void cancelBookmark(Long userId, Long campaignId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        Bookmark bookmark = bookmarkRepository.findByUserAndCampaign(user, campaign)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        bookmark.setIsActive(false);
        bookmarkRepository.save(bookmark);
    }

    @Override
    public BookmarkSplitResponseDTO getBookmarks(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        LocalDate today = LocalDate.now();
        List<BookmarkResponseDTO> open = new ArrayList<>();
        List<BookmarkResponseDTO> closed = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            BookmarkResponseDTO dto = BookmarkResponseDTO.fromEntity(bookmark);
            LocalDate reviewerAnnouncement = bookmark.getCampaign().getReviewerAnnouncement();
            if (reviewerAnnouncement != null && !reviewerAnnouncement.isBefore(today)) {
                open.add(dto);
            } else {
                closed.add(dto);
            }
        }
        Page<BookmarkResponseDTO> openPage = new PageImpl<>(open, pageable, open.size());
        Page<BookmarkResponseDTO> closedPage = new PageImpl<>(closed, pageable, closed.size());
        return BookmarkSplitResponseDTO.builder()
            .open(PageListResponseDTO.from(openPage))
            .closed(PageListResponseDTO.from(closedPage))
            .build();
    }

    @Override
    @Transactional
    public void deleteBookmark(Long userId, Long campaignId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        bookmarkRepository.deleteByUserAndCampaign(user, campaign);
    }
} 