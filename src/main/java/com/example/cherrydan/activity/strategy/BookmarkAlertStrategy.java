package com.example.cherrydan.activity.strategy;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.activity.domain.ActivityAlertType;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.common.util.CompositeAlertIterator;
import com.example.cherrydan.common.util.PagedAlertIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookmarkAlertStrategy implements AlertStrategy {
    
    private final BookmarkRepository bookmarkRepository;
    
    @Override
    public Iterator<ActivityAlert> generateAlertsIterator(LocalDate today) {
        return new CompositeAlertIterator(
            new PagedAlertIterator<>(
                page -> bookmarkRepository.findActiveBookmarksByApplyEndDate(
                    today.plusDays(1), page),  // D-1
                bookmark -> createAlert(bookmark, ActivityAlertType.BOOKMARK_DEADLINE_D1, today)
            ),
            new PagedAlertIterator<>(
                page -> bookmarkRepository.findActiveBookmarksByApplyEndDate(
                    today, page),  // D-Day
                bookmark -> createAlert(bookmark, ActivityAlertType.BOOKMARK_DEADLINE_DDAY, today)
            )
        );
    }
    
    private ActivityAlert createAlert(Bookmark bookmark, ActivityAlertType type, LocalDate date) {
        return ActivityAlert.builder()
            .user(bookmark.getUser())
            .campaign(bookmark.getCampaign())
            .alertType(type)
            .alertDate(date)
            .build();
    }
    
}