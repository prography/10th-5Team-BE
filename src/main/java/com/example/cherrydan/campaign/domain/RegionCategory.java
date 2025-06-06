package com.example.cherrydan.campaign.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 지역 카테고리 enum (모든 지역 공통 사용)
 */
@Getter
@RequiredArgsConstructor
public enum RegionCategory {
    ALL("전체", "전체 카테고리"),
    RESTAURANT("맛집", "맛집/음식점"),
    BEAUTY("뷰티", "뷰티/화장품"),
    ACCOMMODATION("숙박", "숙박/호텔"),
    CULTURE("문화", "문화/엔터테인먼트"),
    DELIVERY("배달", "배달/음식배달"),
    PACKAGING("포장", "포장/테이크아웃"),
    ETC("기타", "기타");

    private final String code;
    private final String description;
}
