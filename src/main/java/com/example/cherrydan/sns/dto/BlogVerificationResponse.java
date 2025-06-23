package com.example.cherrydan.sns.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlogVerificationResponse {
    private String verificationCode;
    private String blogUrl;
    private String instructions;
} 