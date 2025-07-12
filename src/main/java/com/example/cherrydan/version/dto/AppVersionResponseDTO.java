package com.example.cherrydan.version.dto;

import com.example.cherrydan.version.domain.AppVersion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponseDTO {
    private String minSupportedVersion;
    private String latestVersion;

    public static AppVersionResponseDTO from(AppVersion appVersion) {
        return AppVersionResponseDTO.builder()
                .minSupportedVersion(appVersion.getMinSupportedVersion())
                .latestVersion(appVersion.getLatestVersion())
                .build();
    }
}
