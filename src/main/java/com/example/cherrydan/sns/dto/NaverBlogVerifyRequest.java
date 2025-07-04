package com.example.cherrydan.sns.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverBlogVerifyRequest {
    private String code;
    private String blogUrl;
} 