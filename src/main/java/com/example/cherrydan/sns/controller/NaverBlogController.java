package com.example.cherrydan.sns.controller;

import com.example.cherrydan.sns.dto.NaverBlogVerifyRequest;
import com.example.cherrydan.sns.service.NaverBlogService;
import com.example.cherrydan.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.sns.domain.SnsPlatform;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.user.domain.User;

@RestController
@RequestMapping("/api/v1/sns/naver")
@RequiredArgsConstructor
public class NaverBlogController {

    private final NaverBlogService naverBlogService;
    private final UserRepository userRepository;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyNaverBlog(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @RequestBody NaverBlogVerifyRequest request
    ) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(ErrorMessage.USER_NOT_FOUND.getMessage()));
        }

        try {
            String result = naverBlogService.verifyAndSave(
                request.getCode(),
                request.getBlogUrl(),
                user,
                SnsPlatform.NAVER
            );
            if (result != null) {
                return ResponseEntity.ok(ApiResponse.success("블로그 연동 성공", result));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(
                    ErrorMessage.NAVER_BLOG_INVALID_DESCRIPTION.getMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 
