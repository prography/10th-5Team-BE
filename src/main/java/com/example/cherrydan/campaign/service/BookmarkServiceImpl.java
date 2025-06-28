package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

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
    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getBookmarks(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        return bookmarkRepository.findAllByUserAndIsActiveTrue(user).stream()
                .map(BookmarkResponseDTO::fromEntity)
                .collect(Collectors.toList());
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