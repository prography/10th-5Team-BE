package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_push_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserPushSettings extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "pushSettings", fetch = FetchType.LAZY)
    private User user;

    @Builder.Default
    @Column(name = "activity_enabled")
    private Boolean activityEnabled = true;

    @Builder.Default
    @Column(name = "personalized_enabled")
    private Boolean personalizedEnabled = true;

    @Builder.Default
    @Column(name = "service_enabled")
    private Boolean serviceEnabled = true;

    @Builder.Default
    @Column(name = "marketing_enabled")
    private Boolean marketingEnabled = true;

    @Builder.Default
    @Column(name = "push_enabled")
    private Boolean pushEnabled = true;

    public void updateSettings(Boolean activityEnabled, Boolean personalizedEnabled,
                               Boolean serviceEnabled, Boolean marketingEnabled, Boolean pushEnabled) {
        if (activityEnabled != null) this.activityEnabled = activityEnabled;
        if (personalizedEnabled != null) this.personalizedEnabled = personalizedEnabled;
        if (serviceEnabled != null) this.serviceEnabled = serviceEnabled;
        if (marketingEnabled != null) this.marketingEnabled = marketingEnabled;
        if (pushEnabled != null) this.pushEnabled = pushEnabled;
    }
}
