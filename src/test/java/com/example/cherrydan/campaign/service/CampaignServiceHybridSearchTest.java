package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.user.repository.KeywordCampaignAlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignService 하이브리드 검색 전략 테스트")
class CampaignServiceHybridSearchTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private KeywordCampaignAlertRepository keywordCampaignAlertRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("오늘 날짜로 조회 시 FULLTEXT 검색을 사용한다")
    void getPersonalizedCampaigns_WithTodayDate_UseFulltext() {
        // given
        String keyword = "부산";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<Campaign> mockCampaigns = Collections.emptyList();
        Set<Long> mockBookmarks = Collections.emptySet();

        when(campaignRepository.searchDailyCampaignsByFulltext(
            eq("+" + keyword + "*"),
            eq(0),
            eq(10)
        )).thenReturn(mockCampaigns);

        when(campaignRepository.countDailyCampaignsByFulltext(eq(keyword)))
            .thenReturn(0L);

        when(bookmarkRepository.findBookmarkedCampaignIds(eq(userId), anyList()))
            .thenReturn(mockBookmarks);

        // when
        Page<CampaignResponseDTO> result = campaignService.getPersonalizedCampaignsByKeyword(
            keyword, today, userId, pageable
        );

        // then
        verify(campaignRepository, times(1)).searchDailyCampaignsByFulltext(
            eq("+" + keyword + "*"),
            eq(0),
            eq(10)
        );
        verify(campaignRepository, times(1)).countDailyCampaignsByFulltext(eq(keyword));
        verify(campaignRepository, never()).findByKeywordSimpleLike(anyString(), any(LocalDate.class), anyInt(), anyInt());
        verify(campaignRepository, never()).countByKeywordSimpleLike(anyString(), any(LocalDate.class));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("과거 날짜로 조회 시 Simple LIKE 검색을 사용한다")
    void getPersonalizedCampaigns_WithPastDate_UseSimpleLike() {
        // given
        String keyword = "서울";
        LocalDate pastDate = LocalDate.of(2025, 11, 28);
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<Campaign> mockCampaigns = Collections.emptyList();
        Set<Long> mockBookmarks = Collections.emptySet();

        when(campaignRepository.findByKeywordSimpleLike(
            eq(keyword),
            eq(pastDate),
            eq(0),
            eq(10)
        )).thenReturn(mockCampaigns);

        when(campaignRepository.countByKeywordSimpleLike(eq(keyword), eq(pastDate)))
            .thenReturn(0L);

        when(bookmarkRepository.findBookmarkedCampaignIds(eq(userId), anyList()))
            .thenReturn(mockBookmarks);

        // when
        Page<CampaignResponseDTO> result = campaignService.getPersonalizedCampaignsByKeyword(
            keyword, pastDate, userId, pageable
        );

        // then
        verify(campaignRepository, times(1)).findByKeywordSimpleLike(
            eq(keyword),
            eq(pastDate),
            eq(0),
            eq(10)
        );
        verify(campaignRepository, times(1)).countByKeywordSimpleLike(eq(keyword), eq(pastDate));
        verify(campaignRepository, never()).searchDailyCampaignsByFulltext(anyString(), anyInt(), anyInt());
        verify(campaignRepository, never()).countDailyCampaignsByFulltext(anyString());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}