package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.dto.RefreshTokenDTO;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.dto.UserInfoDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.oauth.service.AuthService;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    private final AuthService authService;

    @Operation(
            summary = "토큰 갱신", 
            description = """
                    리프레쉬 토큰을 사용하여 새로운 액세스 토큰과 리프레쉬 토큰을 발급받습니다.
                    보안을 위해 리프레쉬 토큰도 함께 갱신됩니다.
                    """
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDTO>> refresh(@RequestBody RefreshTokenDTO refreshToken) {
        TokenDTO newTokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신이 완료되었습니다.", newTokens));
    }

    @Operation(summary = "로그아웃 처리", description = "현재 로그인한 사용자의 리프레쉬 토큰을 삭제합니다")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser){
        // 현재 사용자의 토큰 삭제 처리
        authService.logout(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}