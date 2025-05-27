package com.example.cherrydan.user.controller;

import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 관련 API")
public class UserController {

    private final UserRepository userRepository;

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT 인증을 통해 현재 로그인한 사용자의 상세 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }

        // DB에서 최신 사용자 정보 조회
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserDetailResponse userDetail = new UserDetailResponse(user);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", userDetail));
    }

    /**
     * 사용자 상세 정보 응답 DTO
     */
    public static class UserDetailResponse {
        public Long id;
        public String email;
        public String name;
        public String picture;
        public String provider;
        public String role;

        public UserDetailResponse(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.picture = user.getPicture();
            this.provider = user.getProvider().name();
            this.role = user.getRole().name();
        }
    }
}
