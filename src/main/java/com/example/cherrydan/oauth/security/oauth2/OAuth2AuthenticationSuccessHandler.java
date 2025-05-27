package com.example.capstone.oauth.security.oauth2;

import com.example.capstone.oauth.security.jwt.JwtTokenProvider;
import com.example.capstone.oauth.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth2.redirect.success-url}")
    private String redirectSuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // JWT 토큰 생성
        String accessToken = tokenProvider.generateToken(authentication);

        // 리프레시 토큰 생성
        String refreshToken = refreshTokenService.createRefreshToken(authentication);

        // 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(redirectSuccessUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 리다이렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
