package com.example.cherrydan.version.dto;

import com.example.cherrydan.version.domain.AppVersion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponseDTO {
    private String min_supported_version;
    private String latest_version;

    public static AppVersionResponseDTO from(AppVersion appVersion) {
        return AppVersionResponseDTO.builder()
                .min_supported_version(appVersion.getMinSupportedVersion())
                .latest_version(appVersion.getLatestVersion())
                .build();
    }
}
