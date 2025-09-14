package com.example.cherrydan.common.util;

import com.example.cherrydan.activity.domain.ActivityAlert;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 여러 Iterator를 하나로 합치는 유틸리티
 * D-1, D-Day 등 여러 조건의 알림을 순차적으로 처리
 */
public class CompositeAlertIterator implements Iterator<ActivityAlert> {

    private final List<Iterator<ActivityAlert>> iterators;
    private int currentIndex = 0;

    @SafeVarargs
    public CompositeAlertIterator(Iterator<ActivityAlert>... iterators) {
        this.iterators = Arrays.asList(iterators);
    }

    @Override
    public boolean hasNext() {
        while (currentIndex < iterators.size()) {
            if (iterators.get(currentIndex).hasNext()) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }

    @Override
    public ActivityAlert next() {
        return iterators.get(currentIndex).next();
    }
 }