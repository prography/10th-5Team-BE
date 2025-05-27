package com.example.cherrydan.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequestDTO {

    private String email;
    private String password;
    private String name;
}
