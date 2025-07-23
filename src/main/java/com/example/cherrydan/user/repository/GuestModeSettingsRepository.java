package com.example.cherrydan.user.repository;

import com.example.cherrydan.user.domain.GuestModeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestModeSettingsRepository extends JpaRepository<GuestModeSettings, Long> {
    
    Optional<GuestModeSettings> findFirstByOrderByIdAsc();
} 