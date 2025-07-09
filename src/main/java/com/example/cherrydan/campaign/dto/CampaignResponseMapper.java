package com.example.cherrydan.campaign.dto;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignResponseMapper {
    public static Set<Long> getBookmarkedCampaignIds(BookmarkRepository bookmarkRepository, Long userId) {
        if (userId != null) {
            return bookmarkRepository.findAllByUserIdAndIsActiveTrue(userId)
                .stream().map(b -> b.getCampaign().getId()).collect(Collectors.toSet());
        } else {
            return java.util.Collections.emptySet();
        }
    }

    public static List<CampaignResponseDTO> toResponseDTOList(List<Campaign> campaigns, Set<Long> bookmarkedCampaignIds) {
        return campaigns.stream()
            .map(campaign -> CampaignResponseDTO.fromEntityWithBookmark(
                campaign,
                bookmarkedCampaignIds.contains(campaign.getId())
            ))
            .collect(Collectors.toList());
    }
} 