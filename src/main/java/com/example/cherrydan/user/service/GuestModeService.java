package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.GuestModeSettings;
import com.example.cherrydan.user.repository.GuestModeSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestModeService {
    
    private final GuestModeSettingsRepository guestModeSettingsRepository;
    
    public boolean isGuestModeEnabled() {
        return guestModeSettingsRepository.findFirstByOrderByIdAsc()
                .map(GuestModeSettings::getIsEnabled)
                .orElse(false);
    }
} 