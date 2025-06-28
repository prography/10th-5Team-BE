package com.example.cherrydan.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Kakao 로그인 요청 DTO")
public class KakaoLoginRequest {
    
    @Schema(description = "Kakao 액세스 토큰", example = "kakao_access_token_string")
    private String accessToken;
} 