package com.example.cherrydan.activity.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityProcessingServiceTest {

    @Test
    @DisplayName("D-day 계산 로직 검증")
    void testDDayCalculation() {
        // given
        LocalDate today = LocalDate.of(2024, 11, 12);
        LocalDate applyEndDate = LocalDate.of(2024, 11, 15);
        
        // when
        long dDay = ChronoUnit.DAYS.between(today, applyEndDate);
        
        // then
        assertThat(dDay).isEqualTo(3L);
    }
    
    @Test
    @DisplayName("D-day 계산 - 34일 차이")
    void testDDayCalculation34Days() {
        // given
        LocalDate today = LocalDate.of(2024, 11, 12);
        LocalDate applyEndDate = LocalDate.of(2024, 12, 16);
        
        // when
        long dDay = ChronoUnit.DAYS.between(today, applyEndDate);
        
        // then
        assertThat(dDay).isEqualTo(34L);
    }
    
    @Test
    @DisplayName("D-day 계산 - 당일")
    void testDDayCalculationSameDay() {
        // given
        LocalDate today = LocalDate.of(2024, 11, 12);
        LocalDate applyEndDate = LocalDate.of(2024, 11, 12);
        
        // when
        long dDay = ChronoUnit.DAYS.between(today, applyEndDate);
        
        // then
        assertThat(dDay).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("D-day 계산 - 과거 날짜")
    void testDDayCalculationPastDate() {
        // given
        LocalDate today = LocalDate.of(2024, 11, 12);
        LocalDate applyEndDate = LocalDate.of(2024, 11, 10);
        
        // when
        long dDay = ChronoUnit.DAYS.between(today, applyEndDate);
        
        // then
        assertThat(dDay).isEqualTo(-2L);
    }
}