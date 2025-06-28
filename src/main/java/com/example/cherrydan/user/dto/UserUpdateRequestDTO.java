package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {
    private String nickname;
    private Integer birthYear;
    private Gender gender;
} 