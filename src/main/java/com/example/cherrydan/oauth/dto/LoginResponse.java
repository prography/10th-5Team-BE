package com.example.cherrydan.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenDTO tokens;
    private Long userId;
}