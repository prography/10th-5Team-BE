package com.example.cherrydan.campaign.domain;

public enum ProductCategory {
    FOOD(1, "식품", "food"),
    LIVING(2, "생활", "living"),
    DIGITAL(3, "디지털", "digital"),
    BEAUTY_FASHION(4, "뷰티/패션", "beauty_fashion"),
    PET(5, "반려동물", "pet"),
    KIDS(6, "유아동", "kids"),
    BOOK(7, "도서", "book"),
    RESTAURANT(8, "맛집", "restaurant"),
    TRAVEL(9, "여행", "travel"),
    SERVICE(10, "서비스", "service"),
    ETC(99, "기타", "etc");

    private final int code;
    private final String label;
    private final String engLabel;

    ProductCategory(int code, String label, String engLabel) {
        this.code = code;
        this.label = label;
        this.engLabel = engLabel;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
    public String getEngLabel() { return engLabel; }

    public static ProductCategory fromString(String value) {
        String trimmedValue = value.trim();
        for (ProductCategory c : values()) {
            if (c.engLabel.equalsIgnoreCase(trimmedValue)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown product category: " + value);
    }
} 