package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.UserTos;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTosResponseDTO {
    private Boolean isAgreedServiceUsage;
    private Boolean isAgreedPrivateInfo;
    private Boolean isAgreed3rdParty;
    private LocalDateTime essentialConsentUpdated;
    private Boolean isAgreedLocationInfo;
    private LocalDateTime locationConsentUpdated;
    private Boolean isAgreedAds;
    private LocalDateTime adsConsentUpdated;

    public static UserTosResponseDTO from(UserTos userTos) {
        return UserTosResponseDTO.builder()
                .isAgreedServiceUsage(userTos.getIsAgreedServiceUsage())
                .isAgreedPrivateInfo(userTos.getIsAgreedPrivateInfo())
                .isAgreed3rdParty(userTos.getIsAgreed3rdParty())
                .essentialConsentUpdated(userTos.getEssentialConsentUpdated())
                .isAgreedLocationInfo(userTos.getIsAgreedLocationInfo())
                .locationConsentUpdated(userTos.getLocationConsentUpdated())
                .isAgreedAds(userTos.getIsAgreedAds())
                .adsConsentUpdated(userTos.getAdsConsentUpdated())
                .build();
    }
} 