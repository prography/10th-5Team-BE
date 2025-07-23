package com.example.cherrydan.push.dto;
import com.example.cherrydan.user.domain.UserPushSettings;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSettingsResponseDTO {
    private Boolean activityEnabled;
    private Boolean personalizedEnabled;
    private Boolean serviceEnabled;
    private Boolean marketingEnabled;
    private Boolean pushEnabled;

    public static PushSettingsResponseDTO from(UserPushSettings settings) {
        return PushSettingsResponseDTO.builder()
                .activityEnabled(settings.getActivityEnabled())
                .personalizedEnabled(settings.getPersonalizedEnabled())
                .serviceEnabled(settings.getServiceEnabled())
                .marketingEnabled(settings.getMarketingEnabled())
                .pushEnabled(settings.getPushEnabled())
                .build();
    }
}
