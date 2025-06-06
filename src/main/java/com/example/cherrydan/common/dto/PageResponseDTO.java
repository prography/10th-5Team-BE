package com.example.cherrydan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;           // 실제 데이터
    private int currentPage;           // 현재 페이지 (0부터 시작)
    private int totalPages;            // 총 페이지 수
    private long totalElements;        // 총 요소 수
    private int size;                  // 페이지 크기
    private int numberOfElements;      // 현재 페이지의 요소 수
    private boolean first;             // 첫 번째 페이지 여부
    private boolean last;              // 마지막 페이지 여부
    private boolean hasNext;           // 다음 페이지 존재 여부
    private boolean hasPrevious;       // 이전 페이지 존재 여부

    /**
     * Spring Page 객체를 PageResponseDTO로 변환
     */
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return PageResponseDTO.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
