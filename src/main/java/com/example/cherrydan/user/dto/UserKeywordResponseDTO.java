package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.UserKeyword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKeywordResponseDTO {
    private Long id;
    private String keyword;

    public static UserKeywordResponseDTO fromKeyword(UserKeyword keyword) {
        return UserKeywordResponseDTO.builder()
                .id(keyword.getId())
                .keyword(keyword.getKeyword())
                .build();
    }
} 