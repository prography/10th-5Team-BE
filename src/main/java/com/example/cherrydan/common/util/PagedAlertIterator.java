package com.example.cherrydan.common.util;

import com.example.cherrydan.activity.domain.ActivityAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 페이징 기반 ActivityAlert Iterator
 * 메모리 효율적인 대량 데이터 처리를 위한 유틸리티
 */
@Slf4j
public class PagedAlertIterator<T> implements Iterator<ActivityAlert> {
    
    private final Function<Pageable, Page<T>> pageLoader;
    private final Function<T, ActivityAlert> alertMapper;
    private Iterator<T> currentIterator = Collections.emptyIterator();
    private int currentPage = 0;
    private boolean hasMorePages = true;
    private static final int PAGE_SIZE = 500;
    
    public PagedAlertIterator(Function<Pageable, Page<T>> pageLoader,
                             Function<T, ActivityAlert> alertMapper) {
        this.pageLoader = pageLoader;
        this.alertMapper = alertMapper;
        loadNextPage();
    }
    
    private void loadNextPage() {
        if (!hasMorePages) {
            return;
        }
        log.info("Loading page: {}", currentPage);
        Page<T> page = pageLoader.apply(PageRequest.of(currentPage++, PAGE_SIZE));
        log.info("Loaded pages: {}, Current page size: {}", page.getTotalPages(), page.getNumberOfElements());
        currentIterator = page.getContent().iterator();
        hasMorePages = page.hasNext();
    }
    
    @Override
    public boolean hasNext() {
        if (currentIterator.hasNext()) {
            return true;
        }
        
        if (hasMorePages) {
            loadNextPage();
            return currentIterator.hasNext();
        }
        
        return false;
    }
    
    @Override
    public ActivityAlert next() {
        return alertMapper.apply(currentIterator.next());
    }
}