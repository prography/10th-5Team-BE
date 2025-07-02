package com.example.cherrydan.oauth.controller;

import com.example.cherrydan.common.response.ApiResponse;

import com.example.cherrydan.oauth.dto.LoginResponse;
import com.example.cherrydan.oauth.dto.NaverLoginRequest;
import com.example.cherrydan.oauth.dto.TokenDTO;
import com.example.cherrydan.oauth.security.jwt.JwtTokenProvider;
import com.example.cherrydan.oauth.security.oauth2.CustomOAuth2UserService;
import com.example.cherrydan.oauth.security.oauth2.user.NaverOAuth2UserInfo;

import com.example.cherrydan.oauth.security.oauth2.user.OAuth2UserInfo;
import com.example.cherrydan.oauth.service.NaverOAuthService;
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
@RequestMapping("/api/auth/naver")
@RequiredArgsConstructor
@Tag(name = "Naver 인증", description = "Naver 로그인 관련 API")
public class NaverAuthController {
    
    private final NaverOAuthService naverOAuthService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Naver 모바일 로그인/회원가입",
            description = """
                    ### Naver 모바일 SDK를 통해 로그인 후, 받은 액세스 토큰으로 서버에 로그인/회원가입을 요청하는 API 입니다.
                    
                    **모바일 클라이언트 개발 순서:**
                    1. 각 플랫폼(iOS/Android)에 맞는 Naver SDK를 사용하여 사용자의 네이버 로그인을 처리합니다.
                    2. 로그인 성공 시, Naver로부터 **액세스 토큰** 문자열을 발급받습니다.
                    3. 발급받은 액세스 토큰을 이 API의 Body에 담아 요청합니다.
                    4. 요청 성공 시, 응답으로 받은 **accessToken**과 **refreshToken**을 앱 내 안전한 곳(e.g., Keychain, Keystore)에 저장합니다.
                    5. 이후 저희 서비스의 다른 API를 호출할 때는, `Authorization` 헤더에 `Bearer {accessToken}` 형식으로 토큰을 담아 요청합니다.
                    
                    ---
                    
                    **iOS (Swift) 요청 예시:**
                    ```swift
                    guard let url = URL(string: "cherrydan.com/api/auth/naver/login") else { return }
                    var request = URLRequest(url: url)
                    request.httpMethod = "POST"
                    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
                    
                    let body = ["accessToken": "naver_access_token_string"]
                    request.httpBody = try? JSONEncoder().encode(body)
                    
                    URLSession.shared.dataTask(with: request) { data, response, error in
                        // Handle response...
                    }.resume()
                    ```
                    
                    **Android (Kotlin with Retrofit) 요청 예시:**
                    ```kotlin
                    // Retrofit Interface
                    interface ApiService {
                        @POST("api/auth/naver/login")
                        suspend fun loginWithNaver(@Body body: NaverLoginRequest): Response<LoginResponse>
                    }
                    
                    // DTO
                    data class NaverLoginRequest(val accessToken: String)
                    
                    // ViewModel or Repository
                    suspend fun performNaverLogin(accessToken: String) {
                        val request = NaverLoginRequest(accessToken = accessToken)
                        val response = yourRetrofitService.loginWithNaver(request)
                        // Handle response...
                    }
                    ```
                    """
    )
    public ResponseEntity<ApiResponse<LoginResponse>> naverMobileLogin(@RequestBody NaverLoginRequest request) {
        // 1. Naver 액세스 토큰으로 사용자 정보 조회
        Map<String, Object> naverUserInfo = naverOAuthService.getUserInfo(request.getAccessToken());
        
        // 2. OAuth2UserInfo 객체 생성
        OAuth2UserInfo oAuth2UserInfo = new NaverOAuth2UserInfo(naverUserInfo);
        
        // 3. 사용자 조회 또는 생성
        User user = customOAuth2UserService.processNaverUser(oAuth2UserInfo, request.getFcmToken(), request.getDeviceType());
        
        // 4. Access Token과 Refresh Token 생성
        TokenDTO tokenDTO = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());
        
        // 5. Refresh Token을 DB에 저장
        refreshTokenService.saveOrUpdateRefreshToken(user.getId(), tokenDTO.getRefreshToken());

        log.info("Naver 모바일 로그인 성공: userId={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());
        
        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(tokenDTO, user.getId())));
    }
} 