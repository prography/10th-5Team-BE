package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.dto.UserInfoDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * Access Token 갱신
     * 클라이언트의 HttpOnly 쿠키에서 Refresh Token을 가져와서 새로운 Access Token 발급
     */
    @Operation(summary = "Access Token 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDTO>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        Optional<User> userOpt = validateToken(refreshToken);

        User user = userOpt.get();
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        
        log.info("Access Token 갱신 완료: 사용자 ID = {}", user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신이 완료되었습니다.", new TokenDTO(newAccessToken)));
    }

    private Optional<User> validateToken(String refreshToken) {
        if (refreshToken == null) {
            throw new AuthException(ErrorMessage.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        // Refresh Token 유효성 검증
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        // 사용자 정보 조회
        Optional<User> userOpt = refreshTokenService.getUserByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            throw new UserException(ErrorMessage.USER_NOT_FOUND);
        }
        return userOpt;
    }

    /**
     * 로그아웃
     * Refresh Token 쿠키 삭제 및 DB에서 Refresh Token 제거
     */
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃시키고 Refresh Token을 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        
        // Refresh Token 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);

        // DB에서 Refresh Token 삭제
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        log.info("로그아웃 완료");
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 현재 사용자 정보 조회
     * JWT 필터에서 인증된 사용자 정보를 직접 가져옴
     */
    @Operation(summary = "현재 사용자 조회", description = "JWT 인증을 통해 현재 로그인한 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }

        UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .name(currentUser.getName())
                .picture(currentUser.getPicture())
                .provider(currentUser.getProvider())
                .build();

        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", userInfo));
    }
}
