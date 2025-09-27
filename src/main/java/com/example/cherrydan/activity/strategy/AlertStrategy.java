package com.example.cherrydan.activity.strategy;

import com.example.cherrydan.activity.domain.ActivityAlert;
import java.time.LocalDate;
import java.util.Iterator;

public interface AlertStrategy {
    /**
     * Iterator 방식으로 알림 생성 (메모리 효율적)
     * 대량 데이터 처리 시 메모리 사용량을 최소화
     */
    Iterator<ActivityAlert> generateAlertsIterator(LocalDate today);
}