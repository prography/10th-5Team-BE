package com.example.capstone.user.service;

import com.example.capstone.common.annotation.LogExecutionTime;
import com.example.capstone.common.exception.BaseException;
import com.example.capstone.common.exception.ErrorMessage;
import com.example.capstone.oauth.dto.UserInfoDTO;
import com.example.capstone.oauth.model.AuthProvider;
import com.example.capstone.oauth.security.jwt.UserDetailsImpl;
import com.example.capstone.user.domain.Role;
import com.example.capstone.user.domain.User;
import com.example.capstone.user.dto.SignUpRequestDTO;
import com.example.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @LogExecutionTime
    public UserInfoDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BaseException(ErrorMessage.USER_NOT_FOUND));
        
        return UserInfoDTO.fromEntity(user);
    }

    @LogExecutionTime
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorMessage.USER_INVALID_CREDENTIALS));
    }

    
    /**
     * 회원가입 처리
     * 
     * @param signUpRequest 회원가입 요청 정보
     * @throws BaseException 이미 존재하는 이메일인 경우
     */
    @Transactional
    @LogExecutionTime
    public void signUp(SignUpRequestDTO signUpRequest) {
        // 이메일 중복 체크
        if (!isEmailAvailable(signUpRequest.getEmail())) {
            throw new BaseException(ErrorMessage.USER_EMAIL_ALREADY_EXISTS);
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        
        // 사용자 객체 생성
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(encodedPassword)
                .name(signUpRequest.getName())
                .picture(null) // 기본 프로필 이미지 없음
                .role(Role.ROLE_USER)
                .provider(AuthProvider.LOCAL) // 일반 회원가입은 LOCAL 제공자로 설정
                .build();
        
        // DB에 저장
        userRepository.save(user);
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복되지 않은 경우 true, 중복된 경우 false
     */
    @LogExecutionTime
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}