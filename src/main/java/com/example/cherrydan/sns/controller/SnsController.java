package com.example.cherrydan.sns.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.SnsConnectionResponse;
import com.example.cherrydan.sns.service.SnsOAuthService;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sns")
@RequiredArgsConstructor
@Tag(name = "SNS", description = "SNS 연동 관련 API")
@Slf4j
public class SnsController {

    private final SnsOAuthService snsOAuthService;
    private final UserService userService;

    @Operation(summary = "OAuth 인증 URL 생성")
    @GetMapping("/oauth/{platform}/auth-url")
    public ApiResponse<String> getAuthUrl(@PathVariable("platform") String platform) {
        SnsPlatform snsPlatform = SnsPlatform.fromPlatformCode(platform);
        String authUrl = snsOAuthService.getAuthUrl(snsPlatform);
        return ApiResponse.success(snsPlatform.getDisplayName() + " 인증 URL 생성 성공", authUrl);
    }

    @Operation(summary = "OAuth 콜백 처리")
    @GetMapping("/oauth/{platform}/callback")
    public Mono<ApiResponse<SnsConnectionResponse>> handleOAuthCallback(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("platform") String platform,
            @RequestParam("code") String code) {
        SnsPlatform snsPlatform = SnsPlatform.fromPlatformCode(platform);
        User user = userService.getUserById(userDetails.getId());
        
        return snsOAuthService.connect(user, code, snsPlatform)
                .map(response -> ApiResponse.success(snsPlatform.getDisplayName() + " 연동이 완료되었습니다.", response));
    }

    @Operation(summary = "사용자 SNS 연동 목록 조회")
    @GetMapping("/connections")
    public ApiResponse<List<SnsConnectionResponse>> getConnections(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        List<SnsConnectionResponse> response = snsOAuthService.getUserSnsConnections(user);
        return ApiResponse.success("SNS 연동 목록 조회 성공", response);
    }

    @Operation(summary = "SNS 연동 해제")
    @DeleteMapping("/disconnect/{platform}")
    public ResponseEntity<ApiResponse<Void>> disconnectSns(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("platform") String platform) {
        SnsPlatform snsPlatform = SnsPlatform.fromPlatformCode(platform);
        User user = userService.getUserById(userDetails.getId());
        snsOAuthService.disconnectSns(user, snsPlatform);
        return ResponseEntity.ok(ApiResponse.success(snsPlatform.getDisplayName() + " 연동이 해제되었습니다.", null));
    }
} 
