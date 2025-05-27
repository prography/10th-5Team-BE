package com.example.capstone.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "인증 토큰 정보")
public class TokenDTO {
    
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", example = "8c1f9458-8ad4-4932-9b9a-6304f52eb375")
    private String refreshToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "토큰 만료 시간(초)", example = "3600")
    private Long expiresIn;
}
