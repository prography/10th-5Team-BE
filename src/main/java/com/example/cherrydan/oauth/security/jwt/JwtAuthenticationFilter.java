package com.example.cherrydan.oauth.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 이미 인증된 사용자는 건너뛰기
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                tokenProvider.validateToken(jwt); // 유효성 검증 (실패 시 예외 발생)

                // Access Token인지 확인
                if (tokenProvider.isAccessToken(jwt)) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    String email = tokenProvider.getEmailFromToken(jwt);

                    // DB 조회 없이 토큰 정보로 UserDetailsImpl 생성
                    UserDetailsImpl userDetails = new UserDetailsImpl(
                            userId,
                            email,
                            null, // name
                            null, // picture
                            null, // provider
                            List.of(new SimpleGrantedAuthority("ROLE_USER")), // 기본 권한
                            null  // attributes
                    );

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("사용자 인증 설정 완료: userId = {}, email = {}", userId, email);
                }
            }
        } catch (Exception ex) {
            log.error("JWT 인증 처리 중 오류 발생: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
