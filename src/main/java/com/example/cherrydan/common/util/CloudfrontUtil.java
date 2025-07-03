package com.example.cherrydan.common.util;

public class CloudfrontUtil {
    public static String getSnsPlatformImageUrl(String sourceSite) {
        String cloudfrontUrl = System.getenv("CLOUDFRONT_URL");
        if (cloudfrontUrl != null && sourceSite != null && !sourceSite.isBlank()) {
            return (cloudfrontUrl.endsWith("/") ? cloudfrontUrl : cloudfrontUrl + "/") + sourceSite + ".png";
        }
        return null;
    }
} 