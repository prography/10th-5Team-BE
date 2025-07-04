package com.example.cherrydan.user.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.oauth.dto.UserInfoDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.dto.UserUpdateRequestDTO;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserInfoDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = getActiveUserById(currentUser.getId());
        return UserInfoDTO.fromEntity(user);
    }

    @Transactional
    public User updateUser(Long userId, String nickname, String email) {
        User user = getActiveUserById(userId);
        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        return user;
    }

    @Transactional
    public User updateUserProfile(Long userId, UserUpdateRequestDTO request) {
        User user = getActiveUserById(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBirthYear() != null) {
            user.setBirthYear(request.getBirthYear());
        }
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            user.setGender(Gender.from(request.getGender()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getActiveUserById(userId);
        user.softDelete(); // 소프트 삭제 적용
        userRepository.save(user);
    }
    
    @Transactional
    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        user.restore();
        userRepository.save(user);
    }
    
    // 활성 사용자만 조회 (기본 메서드)
    public User getUserById(Long id) {
        return getActiveUserById(id);
    }
    
    // 활성 사용자만 조회
    public User getActiveUserById(Long id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }
    
    // 삭제된 사용자 포함 조회 (관리자용)
    public User getUserByIdIncludeDeleted(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return getActiveUserByEmail(email);
    }
    
    // 활성 사용자만 조회
    public User getActiveUserByEmail(String email) {
        return userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_INVALID_CREDENTIALS));
    }
    
    // 삭제된 사용자 포함 조회 (소셜 로그인용)
    public User getUserByEmailIncludeDeleted(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_INVALID_CREDENTIALS));
    }

    /**
     * 이메일 중복 체크 (활성 사용자만)
     *
     * @param email 확인할 이메일
     * @return 중복되지 않은 경우 true, 중복된 경우 false
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsActiveByEmail(email);
    }
    
    /**
     * 삭제된 사용자 포함 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복되지 않은 경우 true, 중복된 경우 false
     */
    public boolean isEmailAvailableIncludeDeleted(String email) {
        return !userRepository.existsByEmail(email);
    }
}