package com.example.cherrydan.campaign.domain;

public enum LocalCategory {
    RESTAURANT(1, "맛집", "restaurant"),
    BEAUTY(2, "뷰티", "beauty"),
    ACCOMMODATION(3, "숙박", "accommodation"),
    CULTURE(4, "문화", "culture"),
    DELIVERY(5, "배달", "delivery"),
    TAKEOUT(6, "포장", "takeout"),
    ETC(99, "기타", "etc");

    private final int code;
    private final String label;
    private final String engLabel;

    LocalCategory(int code, String label, String engLabel) {
        this.code = code;
        this.label = label;
        this.engLabel = engLabel;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
    public String getEngLabel() { return engLabel; }

    public static LocalCategory fromString(String value) {
        String trimmedValue = value.trim();
        for (LocalCategory c : values()) {
            if (c.engLabel.equalsIgnoreCase(trimmedValue)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown local category: " + value);
    }
} 