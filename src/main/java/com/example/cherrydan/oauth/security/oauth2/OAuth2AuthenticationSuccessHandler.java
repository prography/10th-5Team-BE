package com.example.cherrydan.oauth.security.oauth2;

import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.oauth.service.AuthService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${oauth2.redirect.success-url}")
    private String redirectSuccessUrl;
    @Value("${oauth2.redirect.failure-url}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // AuthService를 통해 토큰 생성 (UserDetailsImpl 직접 전달)
            TokenDTO tokenDTO = authService.generateTokens(userDetails);

            log.info("OAuth2 로그인 성공: userId={}, email={}, provider={}", 
                    userDetails.getId(), userDetails.getEmail(), userDetails.getProvider());

            // 리다이렉트 URL에 TokenDTO 정보 포함
            String targetUrl = UriComponentsBuilder.fromUriString(redirectSuccessUrl)
                    .queryParam("accessToken", tokenDTO.getAccessToken())
                    .queryParam("refreshToken", tokenDTO.getRefreshToken())
                    .build().toUriString();

            // 리다이렉트 수행
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage(), e);

            // 오류 발생 시 실패 URL로 리다이렉트
            String errorUrl = UriComponentsBuilder.fromUriString(redirectFailureUrl)
                    .queryParam("error", URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8))
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}