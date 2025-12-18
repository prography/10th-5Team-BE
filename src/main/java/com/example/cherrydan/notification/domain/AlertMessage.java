package com.example.cherrydan.notification.domain;

import java.util.Map;

public interface AlertMessage {
    String title();
    String body();
    Map<String, String> data();

    default String imageUrl() {
        return null;
    }
}
