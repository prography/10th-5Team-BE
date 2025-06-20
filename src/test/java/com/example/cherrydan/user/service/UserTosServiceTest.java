package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserTos;
import com.example.cherrydan.user.dto.UserTosRequestDTO;
import com.example.cherrydan.user.dto.UserTosResponseDTO;
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
class UserTosServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserTosService userTosService;

    @Test
    void getUserTos_createsDefaultIfNotExists() {
        User user = User.builder().id(1L).email("test@email.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserTosResponseDTO response = userTosService.getUserTos(1L);

        assertThat(response.getIsAgreedServiceUsage()).isTrue();
        assertThat(response.getIsAgreedPrivateInfo()).isTrue();
        assertThat(response.getIsAgreed3rdParty()).isFalse();
        assertThat(response.getIsAgreedLocationInfo()).isFalse();
        assertThat(response.getIsAgreedAds()).isFalse();
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUserTos_updatesFields() {
        User user = User.builder().id(1L).email("test@email.com").build();
        UserTos userTos = UserTos.builder().isAgreedServiceUsage(true).isAgreedPrivateInfo(true).build();
        user.setUserTos(userTos);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserTosRequestDTO request = new UserTosRequestDTO(false, false, true, true, true);
        UserTosResponseDTO response = userTosService.updateUserTos(1L, request);

        assertThat(response.getIsAgreedServiceUsage()).isFalse();
        assertThat(response.getIsAgreedPrivateInfo()).isFalse();
        assertThat(response.getIsAgreed3rdParty()).isTrue();
        assertThat(response.getIsAgreedLocationInfo()).isTrue();
        assertThat(response.getIsAgreedAds()).isTrue();
    }
} 