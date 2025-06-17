package com.example.cherrydan.version.dto;

import com.example.cherrydan.version.domain.AppVersion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponseDTO {
    private Long id;
    private String version;
    private String description;
    private LocalDateTime createdAt;

    public static AppVersionResponseDTO from(AppVersion appVersion) {
        return AppVersionResponseDTO.builder()
                .id(appVersion.getId())
                .version(appVersion.getVersion())
                .description(appVersion.getDescription())
                .createdAt(appVersion.getCreatedAt())
                .build();
    }
}
