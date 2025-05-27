package com.example.cherrydan.oauth.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Value("${oauth2.redirect.failure-url}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException {
        
        String errorMessage = exception.getLocalizedMessage();
        log.error("OAuth2 인증 실패: {}", errorMessage, exception);
        
        String targetUrl = redirectFailureUrl + "?message=" +
                          URLEncoder.encode("OAuth 인증에 실패했습니다: " + errorMessage, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
