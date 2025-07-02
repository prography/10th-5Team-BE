package com.example.cherrydan.push.service;

import com.example.cherrydan.push.dto.PushSettingsRequestDTO;
import com.example.cherrydan.push.dto.PushSettingsResponseDTO;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushSettingsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PushSettingsService pushSettingsService;

    @Test
    void createAndGetPushSettings() {
        User user = User.builder().id(1L).email("push@email.com").isActive(true).build();
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

        PushSettingsResponseDTO response = pushSettingsService.getUserPushSettings(1L);

        assertThat(response.getActivityEnabled()).isTrue();
        assertThat(response.getPersonalizedEnabled()).isTrue();
        assertThat(response.getServiceEnabled()).isTrue();
        assertThat(response.getMarketingEnabled()).isTrue();
        assertThat(response.getPushEnabled()).isTrue();
        verify(userRepository, times(1)).findActiveById(1L);
    }

    @Test
    void updatePushSettings() {
        User user = User.builder().id(1L).email("push@email.com").isActive(true).build();
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

        PushSettingsRequestDTO request = new PushSettingsRequestDTO(false, false, true, false, false);
        PushSettingsResponseDTO response = pushSettingsService.updatePushSettings(1L, request);

        assertThat(response.getActivityEnabled()).isFalse();
        assertThat(response.getPersonalizedEnabled()).isFalse();
        assertThat(response.getServiceEnabled()).isTrue();
        assertThat(response.getMarketingEnabled()).isFalse();
        assertThat(response.getPushEnabled()).isFalse();
    }
} 