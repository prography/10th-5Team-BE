package com.example.cherrydan.oauth.security.oauth2;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.oauth.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfoFactory;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 커스텀 OAuth2 사용자 서비스
 * Custom OAuth2 user service that processes OAuth2 login and user registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 정보 로드
     * Load OAuth2 user information from the provider
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (OAuth2AuthenticationProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 오류 발생: {}", ex.getMessage());
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * OAuth2 사용자 정보 처리
     * Process OAuth2 user information and register or update user
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // 등록 ID 및 사용자 정보 가져오기 (Get registration ID and user info)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        // 이메일 확인 (Validate email)
        validateEmail(oAuth2UserInfo);

        // 사용자 조회 또는 생성 (Find or create user)
        User user = findOrCreateUser(oAuth2UserInfo, registrationId);

        // UserDetails 객체 생성 및 반환 (Build and return UserDetails)
        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }

    /**
     * Apple 사용자 정보 처리 (Apple용 별도 메서드)
     * Process Apple user information manually (not through OAuth2 flow)
     */
    public User processAppleUser(OAuth2UserInfo appleUserInfo) {
        try {
            // 이메일 확인 (Validate email)
            validateEmail(appleUserInfo);

            // 사용자 조회 또는 생성 (Find or create user)
            return findOrCreateUser(appleUserInfo, "apple");
        } catch (OAuth2AuthenticationProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Apple 사용자 처리 중 오류 발생: {}", ex.getMessage());
            throw new AuthException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 이메일 유효성 검증
     * Validate email from OAuth2 provider
     */
    private void validateEmail(OAuth2UserInfo oAuth2UserInfo) {
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 제공자로부터 이메일을 찾을 수 없습니다");
        }
    }

    /**
     * 사용자 조회 또는 생성
     * Find existing user or create a new one
     */
    private User findOrCreateUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        // 먼저 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());

        // 이메일로 사용자를 찾았으면
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            
            // 같은 제공자가 아니면 오류 발생
            if (existingUser.getProvider() != null && !existingUser.getProvider().equals(provider)) {
                throw new OAuth2AuthenticationProcessingException(
                        String.format("이미 %s 계정으로 가입되어 있습니다. %s 계정으로 로그인해 주세요.", 
                        existingUser.getProvider(), existingUser.getProvider())
                );
            }
            
            // 정보 업데이트 후 반환
            return updateExistingUser(existingUser, oAuth2UserInfo);
        } else {
            // 이메일로 사용자를 찾지 못했으면 새로 등록
            return registerNewUser(provider, oAuth2UserInfo);
        }
    }

    /**
     * 신규 사용자 등록
     * Register a new user from OAuth2 information
     */
    private User registerNewUser(AuthProvider provider, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .picture(oAuth2UserInfo.getImageUrl())
                .socialId(oAuth2UserInfo.getId())
                .provider(provider)
                .build();

        log.info("새 OAuth2 사용자 등록: {}", user.getEmail());
        return userRepository.save(user);
    }

    /**
     * 기존 사용자 정보 업데이트
     * Update existing user information
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.updateOAuth2Info(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());

        log.info("기존 OAuth2 사용자 정보 업데이트: {}", existingUser.getEmail());
        return userRepository.save(existingUser);
    }
}
