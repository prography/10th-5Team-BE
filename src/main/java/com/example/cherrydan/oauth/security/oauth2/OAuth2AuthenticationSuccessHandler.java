package com.example.cherrydan.oauth.security.oauth2;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.OAuthException;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfoFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        try {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            
            String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            // AuthProvider 매핑
            AuthProvider authProvider;
            try {
                authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("지원하지 않는 OAuth 제공자: {}", registrationId);
                throw new OAuthException(ErrorMessage.OAUTH_PROVIDER_NOT_SUPPORTED);
            }
            
            // OAuth2 제공자별 사용자 정보 추출
            OAuth2UserInfo oAuth2UserInfo;
            try {
                oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oAuth2User.getAttributes());
            } catch (Exception e) {
                log.error("OAuth 사용자 정보 추출 실패: {}", e.getMessage());
                throw new OAuthException(ErrorMessage.OAUTH_USER_INFO_NOT_FOUND);
            }
            
            // 중복 회원가입 방지 검사
            if (userRepository.existsByEmailAndProviderNot(oAuth2UserInfo.getEmail(), authProvider)) {
                log.warn("중복 이메일로 OAuth 로그인 시도: email={}, provider={}", oAuth2UserInfo.getEmail(), authProvider);
                String errorUrl = "http://localhost:3000/api/auth/error?message=" + 
                    URLEncoder.encode("이미 다른 소셜 계정으로 가입된 이메일입니다.", StandardCharsets.UTF_8);
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }
            
            // 사용자 조회 또는 생성
            User user = userRepository.findByProviderAndProviderId(authProvider, oAuth2UserInfo.getId())
                    .map(existingUser -> {
                        // 기존 사용자 정보 업데이트
                        existingUser.updateOAuth2Info(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());
                        User savedUser = userRepository.save(existingUser);
                        log.info("기존 사용자 정보 업데이트: ID = {}, 이메일 = {}", savedUser.getId(), savedUser.getEmail());
                        return savedUser;
                    })
                    .orElseGet(() -> {
                        // 새 사용자 생성 (자동 회원가입)
                        User newUser = User.createOAuthUser(
                                oAuth2UserInfo.getEmail(),
                                oAuth2UserInfo.getName(),
                                oAuth2UserInfo.getImageUrl(),
                                authProvider,
                                oAuth2UserInfo.getId()
                        );
                        User savedUser = userRepository.save(newUser);
                        log.info("신규 사용자 자동 회원가입: ID = {}, 이메일 = {}, 제공자 = {}", 
                                savedUser.getId(), savedUser.getEmail(), authProvider);
                        return savedUser;
                    });

            // JWT 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getId());
            
            // Refresh Token을 DB에 저장
            refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
            
            // Refresh Token을 HttpOnly 쿠키로 설정
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true); // HTTPS에서만 전송
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
            response.addCookie(refreshCookie);
            
            // Access Token은 URL 파라미터로 전달 (프론트엔드에서 받아서 메모리에 저장)
            String redirectUrl = "http://localhost:3000/api/auth/callback?token=" + 
                               URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            
            log.info("OAuth2 로그인 성공: 사용자 ID = {}, 이메일 = {}, 제공자 = {}", 
                    user.getId(), user.getEmail(), authProvider);
            
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            
        } catch (OAuthException e) {
            log.error("OAuth 인증 실패: {}", e.getMessage());
            String errorUrl = "http://localhost:3000/api/auth/error?message=" + 
                URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        } catch (Exception e) {
            log.error("OAuth 인증 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            String errorUrl = "http://localhost:3000/api/auth/error?message=" + 
                URLEncoder.encode("로그인 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
