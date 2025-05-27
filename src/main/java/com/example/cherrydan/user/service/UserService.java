package com.example.cherrydan.user.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.dto.UserInfoDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.domain.Role;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.dto.SignUpRequestDTO;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserInfoDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        return UserInfoDTO.fromEntity(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_INVALID_CREDENTIALS));
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복되지 않은 경우 true, 중복된 경우 false
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}