package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserTos extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "userTos", fetch = FetchType.LAZY)
    private User user;

    @Builder.Default
    @Column(name = "is_agreed_service_usage")
    private Boolean isAgreedServiceUsage = true;

    @Builder.Default
    @Column(name = "is_agreed_private_info")
    private Boolean isAgreedPrivateInfo = true;

    @Builder.Default
    @Column(name = "is_agreed_3rd_party")
    private Boolean isAgreed3rdParty = false;

    @Column(name = "essential_consent_updated")
    private LocalDateTime essentialConsentUpdated;

    @Builder.Default
    @Column(name = "is_agreed_location_info")
    private Boolean isAgreedLocationInfo = false;

    @Column(name = "location_consent_updated")
    private LocalDateTime locationConsentUpdated;

    @Builder.Default
    @Column(name = "is_agreed_ads")
    private Boolean isAgreedAds = false;

    @Column(name = "ads_consent_updated")
    private LocalDateTime adsConsentUpdated;

    /**
     * 필수 동의사항 업데이트
     */
    public void updateEssentialConsent(Boolean serviceUsage, Boolean privateInfo, Boolean thirdParty) {
        if (serviceUsage != null) this.isAgreedServiceUsage = serviceUsage;
        if (privateInfo != null) this.isAgreedPrivateInfo = privateInfo;
        if (thirdParty != null) this.isAgreed3rdParty = thirdParty;
        
        if (serviceUsage != null || privateInfo != null || thirdParty != null) {
            this.essentialConsentUpdated = LocalDateTime.now();
        }
    }

    /**
     * 위치정보 동의 업데이트
     */
    public void updateLocationConsent(Boolean locationInfo) {
        if (locationInfo != null) {
            this.isAgreedLocationInfo = locationInfo;
            this.locationConsentUpdated = LocalDateTime.now();
        }
    }

    /**
     * 마케팅 정보 동의 업데이트
     */
    public void updateAdsConsent(Boolean ads) {
        if (ads != null) {
            this.isAgreedAds = ads;
            this.adsConsentUpdated = LocalDateTime.now();
        }
    }
} 