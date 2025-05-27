package com.example.capstone.oauth.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 제공자 유형")
public enum AuthProvider {
    @Schema(description = "자체 회원가입") LOCAL,
    @Schema(description = "Google 로그인") GOOGLE,
    @Schema(description = "GitHub 로그인") GITHUB,
    @Schema(description = "Kakao 로그인") KAKAO,
    @Schema(description = "Naver 로그인") NAVER
}
