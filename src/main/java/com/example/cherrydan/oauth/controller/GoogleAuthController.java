package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.response.ApiResponse;

import com.example.cherrydan.oauth.dto.GoogleLoginRequest;
import com.example.cherrydan.oauth.dto.LoginResponse;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.oauth2.CustomOAuth2UserService;
import com.example.cherrydan.oauth.security.oauth2.user.GoogleOAuth2UserInfo;

import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.GoogleIdentityTokenService;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google 인증", description = "Google Sign-In 관련 API")
public class GoogleAuthController {
    private final GoogleIdentityTokenService googleIdentityTokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Google 모바일 로그인/회원가입",
            description = """
                    ### Google 모바일 SDK를 통해 로그인 후, 받은 ID Token으로 서버에 로그인/회원가입을 요청하는 API 입니다.
                    
                    **모바일 클라이언트 개발 순서:**
                    1.  각 플랫폼(iOS/Android)에 맞는 Google Sign-In SDK를 사용하여 사용자의 구글 로그인을 처리합니다.
                    2.  로그인 성공 시, Google로부터 **ID Token** 문자열을 발급받습니다.
                    3.  발급받은 ID Token을 이 API의 Body에 담아 요청합니다.
                    4.  요청 성공 시, 응답으로 받은 **accessToken**과 **refreshToken**을 앱 내 안전한 곳(e.g., Keychain, Keystore)에 저장합니다.
                    5.  이후 저희 서비스의 다른 API를 호출할 때는, `Authorization` 헤더에 `Bearer {accessToken}` 형식으로 토큰을 담아 요청합니다.
                    
                    ---
                    
                    **iOS (Swift) 요청 예시:**
                    ```swift
                    guard let url = URL(string: "cherrydan.com/api/auth/google/login") else { return }
                    var request = URLRequest(url: url)
                    request.httpMethod = "POST"
                    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
                    
                    let body = ["idToken": "google_id_token_string"]
                    request.httpBody = try? JSONEncoder().encode(body)
                    
                    URLSession.shared.dataTask(with: request) { data, response, error in
                        // Handle response...
                    }.resume()
                    ```
                    
                    **Android (Kotlin with Retrofit) 요청 예시:**
                    ```kotlin
                    // Retrofit Interface
                    interface ApiService {
                        @POST("api/auth/google/login")
                        suspend fun loginWithGoogle(@Body body: GoogleLoginRequest): Response<LoginResponse>
                    }
                    
                    // DTO
                    data class GoogleLoginRequest(val idToken: String)
                    
                    // ViewModel or Repository
                    suspend fun performGoogleLogin(idToken: String) {
                        val request = GoogleLoginRequest(idToken = idToken)
                        val response = yourRetrofitService.loginWithGoogle(request)
                        // Handle response...
                    }
                    ```
                    """
    )
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody GoogleLoginRequest request) {
        // 1. Google ID Token 검증
        GoogleIdToken.Payload payload = googleIdentityTokenService.verify(request.getAccessToken());

        // 2. OAuth2UserInfo 객체 생성
        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(payload);

        // 3. 사용자 조회 또는 생성
        User user = customOAuth2UserService.processGoogleUser(oAuth2UserInfo, request);

        // 4. Access Token과 Refresh Token 생성
        TokenDTO tokenDTO = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());

        // 5. Refresh Token을 DB에 저장
        refreshTokenService.saveOrUpdateRefreshToken(user.getId(), tokenDTO.getRefreshToken());

        log.info("Google 모바일 로그인 성공: userId={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());

        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(tokenDTO, user.getId())));
    }
}