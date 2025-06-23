package com.example.cherrydan.sns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BlogVerificationData {
    private final Long userId;
    private final String blogUrl;
    private final LocalDateTime createdAt;
} 