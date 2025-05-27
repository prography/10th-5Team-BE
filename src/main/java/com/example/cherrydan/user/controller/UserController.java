package com.example.capstone.user.controller;

import com.example.capstone.common.exception.BaseException;
import com.example.capstone.common.exception.ErrorMessage;
import com.example.capstone.common.response.ApiResponse;
import com.example.capstone.oauth.dto.TokenDTO;
import com.example.capstone.oauth.dto.UserInfoDTO;
import com.example.capstone.oauth.security.jwt.UserDetailsImpl;
import com.example.capstone.oauth.service.AuthService;
import com.example.capstone.user.dto.SignUpRequestDTO;
import com.example.capstone.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 인증 및 정보 관련 API")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "인증된 사용자의 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        UserInfoDTO userInfo = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenDTO>> refreshToken(
            @Parameter(description = "리프레시 토큰", required = true) 
            @RequestParam String refreshToken
    ) {
        TokenDTO tokenDTO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(tokenDTO));
    }
    
    @Operation(
        summary = "이메일 회원가입",
        description = "이메일과 비밀번호를 사용한 일반 회원가입을 처리합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequest) {
        userService.signUp(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("회원가입에 성공했습니다.", null));
    }

    @Operation(
        summary = "이메일 중복 체크",
        description = "회원가입 시 이메일 중복 여부를 확인합니다. 사용 가능한 이메일인 경우 200, 이미 사용 중인 이메일인 경우 409 상태코드를 반환합니다."
    )
    @GetMapping("/check-email")
    public ResponseEntity<Void> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        
        if (isAvailable) {
            return ResponseEntity.ok().build();
        } else {
            throw new BaseException(ErrorMessage.USER_EMAIL_ALREADY_EXISTS);
        }
    }
}