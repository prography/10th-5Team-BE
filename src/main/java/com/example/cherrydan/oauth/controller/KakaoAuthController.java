package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.response.ApiResponse;

import com.example.cherrydan.oauth.dto.KakaoLoginRequest;
import com.example.cherrydan.oauth.dto.LoginResponse;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.oauth2.CustomOAuth2UserService;
import com.example.cherrydan.oauth.security.oauth2.user.KakaoOAuth2UserInfo;

import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.KakaoOAuthService;
import com.example.cherrydan.oauth.service.RefreshTokenService;
import com.example.cherrydan.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "Kakao 인증", description = "Kakao 로그인 관련 API")
public class KakaoAuthController {
    
    private final KakaoOAuthService kakaoOAuthService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Kakao 모바일 로그인/회원가입",
            description = """
                    ### Kakao 모바일 SDK를 통해 로그인 후, 받은 액세스 토큰으로 서버에 로그인/회원가입을 요청하는 API 입니다.
                    
                    **모바일 클라이언트 개발 순서:**
                    1. 각 플랫폼(iOS/Android)에 맞는 Kakao SDK를 사용하여 사용자의 카카오 로그인을 처리합니다.
                    2. 로그인 성공 시, Kakao로부터 **액세스 토큰** 문자열을 발급받습니다.
                    3. 발급받은 액세스 토큰을 이 API의 Body에 담아 요청합니다.
                    4. 요청 성공 시, 응답으로 받은 **accessToken**과 **refreshToken**을 앱 내 안전한 곳(e.g., Keychain, Keystore)에 저장합니다.
                    5. 이후 저희 서비스의 다른 API를 호출할 때는, `Authorization` 헤더에 `Bearer {accessToken}` 형식으로 토큰을 담아 요청합니다.
                    
                    ---
                    
                    **iOS (Swift) 요청 예시:**
                    ```swift
                    guard let url = URL(string: "cherrydan.com/api/auth/kakao/login") else { return }
                    var request = URLRequest(url: url)
                    request.httpMethod = "POST"
                    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
                    
                    let body = ["accessToken": "kakao_access_token_string"]
                    request.httpBody = try? JSONEncoder().encode(body)
                    
                    URLSession.shared.dataTask(with: request) { data, response, error in
                        // Handle response...
                    }.resume()
                    ```
                    
                    **Android (Kotlin with Retrofit) 요청 예시:**
                    ```kotlin
                    // Retrofit Interface
                    interface ApiService {
                        @POST("api/auth/kakao/login")
                        suspend fun loginWithKakao(@Body body: KakaoLoginRequest): Response<LoginResponse>
                    }
                    
                    // DTO
                    data class KakaoLoginRequest(val accessToken: String)
                    
                    // ViewModel or Repository
                    suspend fun performKakaoLogin(accessToken: String) {
                        val request = KakaoLoginRequest(accessToken = accessToken)
                        val response = yourRetrofitService.loginWithKakao(request)
                        // Handle response...
                    }
                    ```
                    """
    )
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoMobileLogin(@RequestBody KakaoLoginRequest request) {
        // 1. Kakao 액세스 토큰으로 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = kakaoOAuthService.getUserInfo(request.getAccessToken());
        
        // 2. OAuth2UserInfo 객체 생성
        OAuth2UserInfo oAuth2UserInfo = new KakaoOAuth2UserInfo(kakaoUserInfo);
        
        // 3. 사용자 조회 또는 생성
        User user = customOAuth2UserService.processKakaoUser(oAuth2UserInfo, request.getFcmToken(), request.getDeviceType());
        
        // 4. Access Token과 Refresh Token 생성
        TokenDTO tokenDTO = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());
        
        // 5. Refresh Token을 DB에 저장
        refreshTokenService.saveOrUpdateRefreshToken(user.getId(), tokenDTO.getRefreshToken());
        
        log.info("Kakao 모바일 로그인 성공: userId={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());
        
        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(tokenDTO, user.getId())));
    }
} 