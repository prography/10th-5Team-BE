package com.example.cherrydan.user.controller;

import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.EmptyResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.dto.UserDto;
import com.example.cherrydan.user.dto.UserKeywordResponseDTO;
import com.example.cherrydan.user.dto.UserUpdateRequestDTO;
import com.example.cherrydan.user.service.UserKeywordService;
import com.example.cherrydan.user.service.UserService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 관련 API")
public class UserController {
    private final UserService userService;
    private final UserKeywordService userKeywordService;

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT 인증을 통해 현재 로그인한 사용자의 상세 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        User user = userService.getUserById(currentUser.getId());
        UserDto userDto = new UserDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", userDto));
    }

    @Operation(
        summary = "사용자 프로필 수정",
        description = "닉네임, 출생연도, 성별 수정 가능",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody UserUpdateRequestDTO request) {
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        User user = userService.updateUserProfile(currentUser.getId(), request);
        UserDto userDto = new UserDto(user);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 수정되었습니다.", userDto));
    }

    @Operation(
        summary = "사용자 탈퇴",
        description = "현재 로그인한 사용자를 탈퇴 처리합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("사용자 탈퇴가 완료되었습니다."));
    }

    @Operation(
        summary = "내 키워드 목록 조회", 
        description = """
            사용자가 등록한 키워드 목록을 조회합니다.
            
            **쿼리 파라미터 예시:**
            - ?page=0&size=20
            - ?page=1&size=10
            
            **정렬**: 키워드 등록 시각 내림차순 (고정)

            """,
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me/keywords")
    public ResponseEntity<ApiResponse<PageListResponseDTO<UserKeywordResponseDTO>>> getMyKeywords(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(description = "페이지네이션 정보")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        Page<UserKeywordResponseDTO> keywords = userKeywordService.getKeywords(currentUser.getId(), pageable)
            .map(UserKeywordResponseDTO::fromKeyword);
        PageListResponseDTO<UserKeywordResponseDTO> response = PageListResponseDTO.from(keywords);
        return ResponseEntity.ok(ApiResponse.success("키워드 목록 조회 성공", response));
    }

    @Operation(summary = "내 키워드 등록", security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/me/keywords")
    public ResponseEntity<ApiResponse<EmptyResponse>> addMyKeyword(@AuthenticationPrincipal UserDetailsImpl currentUser, @RequestParam("keyword") String keyword) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        userKeywordService.addKeyword(currentUser.getId(), keyword);
        return ResponseEntity.ok(ApiResponse.success("키워드 등록 성공"));
    }

    @Operation(summary = "내 키워드 삭제", security = { @SecurityRequirement(name = "bearerAuth") })
    @DeleteMapping("/me/keywords/{keywordId}")
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteMyKeyword(@AuthenticationPrincipal UserDetailsImpl currentUser, @PathVariable Long keywordId) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        userKeywordService.removeKeywordById(currentUser.getId(), keywordId);
        return ResponseEntity.ok(ApiResponse.success("키워드 삭제 성공"));
    }

    @Operation(
        summary = "[관리자] 사용자 계정 복구",
        description = "소프트 삭제된 사용자 계정을 복구합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping("/admin/restore/{userId}")
    public ResponseEntity<ApiResponse<EmptyResponse>> restoreUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable("userId") Long userId) {
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        
        // 관리자 권한 확인 (현재는 생략, 필요시 Role.ADMIN 체크 추가)
        
        userService.restoreUser(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정이 복구되었습니다."));
    }
}
