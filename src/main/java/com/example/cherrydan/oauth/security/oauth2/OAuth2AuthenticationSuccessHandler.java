package com.example.cherrydan.oauth.security.oauth2;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.OAuthException;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth2.redirect.success-url}")
    private String redirectSuccessUrl;
    @Value("${oauth2.redirect.failure-url}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            String userEmail = userDetails.getEmail();

            // 액세스 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(userId, userEmail);

            // 기존 리프레시 토큰이 있는지 확인
            Optional<String> existingRefreshToken = refreshTokenService.findRefreshTokenByUserId(userId);
            String refreshToken;

            if (existingRefreshToken.isPresent() && tokenProvider.validateToken(existingRefreshToken.get())) {
                // 유효한 리프레시 토큰이 있으면 재사용
                refreshToken = existingRefreshToken.get();
                log.info("기존 리프레시 토큰 재사용: 사용자 ID = {}", userId);
            } else {
                // 없거나 만료된 경우 새로 생성
                refreshToken = tokenProvider.generateRefreshToken(userId);
                // 새 리프레시 토큰을 DB에 저장
                refreshTokenService.saveRefreshToken(userId, refreshToken);
                log.info("새 리프레시 토큰 생성: 사용자 ID = {}", userId);
            }

            // Refresh Token을 HttpOnly 쿠키로 설정
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
            response.addCookie(refreshCookie);

            // Access Token은 URL 파라미터로 전달
            String redirectUrl = redirectSuccessUrl + "?token=" +
                    URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

            log.info("OAuth2 로그인 성공: 사용자 ID = {}, 이메일 = {}, 제공자 = {}",
                    userId, userEmail, userDetails.getProvider());

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth 인증 성공 처리 중 오류 발생: {}", e.getMessage(), e);
            String errorUrl = redirectFailureUrl + "?message=" +
                    URLEncoder.encode("로그인 성공 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
