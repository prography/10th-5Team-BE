package com.example.cherrydan.utils;

import org.springframework.stereotype.Component;

public class MaskingUtil {
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        return local.substring(0, Math.min(2, local.length())) +
                "*".repeat(Math.max(0, local.length() - 2)) + "@" + domain;
    }
    public static String maskMdn(String mdn) {
        return maskPhone(mdn);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}