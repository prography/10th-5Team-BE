package com.example.cherrydan.common.util;

public class StringUtil {
    public static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
} 