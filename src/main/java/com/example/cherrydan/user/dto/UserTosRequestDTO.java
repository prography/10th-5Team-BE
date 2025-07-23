package com.example.cherrydan.user.dto;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTosRequestDTO {
    private Boolean isAgreedServiceUsage;
    private Boolean isAgreedPrivateInfo;
    private Boolean isAgreed3rdParty;
    private Boolean isAgreedLocationInfo;
    private Boolean isAgreedAds;
} 