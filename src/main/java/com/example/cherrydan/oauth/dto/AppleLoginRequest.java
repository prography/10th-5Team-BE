package com.example.cherrydan.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Apple 로그인 요청")
public class AppleLoginRequest {
    
    @Schema(description = "Apple Identity Token (JWT)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String identityToken;
}
