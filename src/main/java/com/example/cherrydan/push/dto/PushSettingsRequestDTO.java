package com.example.cherrydan.push.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushSettingsRequestDTO {
    private Boolean activityEnabled;
    private Boolean personalizedEnabled;
    private Boolean serviceEnabled;
    private Boolean marketingEnabled;
    private Boolean pushEnabled;
}
