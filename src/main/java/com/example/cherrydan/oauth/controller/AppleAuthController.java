package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.dto.AppleLoginRequest;
import com.example.cherrydan.oauth.dto.LoginResponse;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.oauth2.CustomOAuth2UserService;
import com.example.cherrydan.oauth.security.oauth2.user.AppleOAuth2UserInfo;
import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.AppleIdentityTokenService;
import com.example.cherrydan.oauth.service.AuthService;
import com.example.cherrydan.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/apple")
@RequiredArgsConstructor
@Tag(name = "Apple 인증", description = "Apple Sign in with Apple 관련 API")
public class AppleAuthController {

    private final AppleIdentityTokenService appleIdentityTokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Apple 로그인", description = "iOS에서 받은 Identity Token으로 Apple 로그인 처리")
    public ResponseEntity<ApiResponse<LoginResponse>> appleLogin(@RequestBody AppleLoginRequest request) {
        // 1. 입력 값 검증
        validateAppleLoginRequest(request);
        
        // 2. Apple Identity Token 검증
        Map<String, Object> userInfo = appleIdentityTokenService.verifyIdentityToken(request.getAccessToken());
        
        // 3. OAuth2UserInfo 객체 생성 (JWT 정보 + iOS 정보 결합)
        OAuth2UserInfo oAuth2UserInfo = new AppleOAuth2UserInfo(userInfo);
        
        // 4. 사용자 조회 또는 생성 (CustomOAuth2UserService 사용)
        User user = customOAuth2UserService.processAppleUser(oAuth2UserInfo);
        
        // 5. Access Token과 Refresh Token 생성
        TokenDTO tokenDTO = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());
        
        log.info("Apple 로그인 성공: userId={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());

        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(tokenDTO,user.getId())));
    }

    /**
     * Apple 로그인 요청 검증
     */
    private void validateAppleLoginRequest(AppleLoginRequest request) {
        if (request == null) {
            throw new AuthException(ErrorMessage.INVALID_REQUEST);
        }
        
        if (!StringUtils.hasText(request.getAccessToken())) {
            throw new AuthException(ErrorMessage.APPLE_USER_INFO_MISSING);
        }
        
        // JWT 형식 기본 검증 (3개 파트로 구성되어야 함)
        String[] tokenParts = request.getAccessToken().split("\\.");
        if (tokenParts.length != 3) {
            throw new AuthException(ErrorMessage.APPLE_IDENTITY_TOKEN_INVALID);
        }
    }
}
