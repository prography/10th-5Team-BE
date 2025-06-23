package com.example.cherrydan.sns.service;

import com.example.cherrydan.common.exception.SnsException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.sns.client.BlogVerificationClient;
import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.dto.BlogVerificationData;
import com.example.cherrydan.sns.dto.BlogVerificationResponse;
import com.example.cherrydan.sns.dto.SnsConnectionResponse;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 블로그 인증 서비스
 * 네이버 블로그 인증을 통해 SNS 연동을 처리합니다.
 * 현재 개발 중이므로 로컬/개발 환경에서만 활성화
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@org.springframework.context.annotation.Profile({"local", "dev", "test"})
public class BlogVerificationService {

    private final SnsConnectionRepository snsConnectionRepository;
    private final BlogVerificationClient blogVerificationClient;

    private final Map<String, BlogVerificationData> verificationStore = new ConcurrentHashMap<>();

    /**
     * 블로그 인증 코드를 생성합니다.
     * @param user 사용자
     * @param blogUrl 블로그 URL
     * @return 인증 응답
     */
    public BlogVerificationResponse generateVerificationCode(User user, String blogUrl) {
        validateBlogUrl(blogUrl);
        
        String verificationCode = generateUniqueCode();
        verificationStore.put(verificationCode, new BlogVerificationData(user.getId(), blogUrl, LocalDateTime.now()));
        
        String instructions = createInstructions(verificationCode);
        
        log.info("블로그 인증 코드 생성: user={}, blogUrl={}, code={}", user.getId(), blogUrl, verificationCode);
        
        return BlogVerificationResponse.builder()
                .verificationCode(verificationCode)
                .blogUrl(blogUrl)
                .instructions(instructions)
                .build();
    }

    /**
     * 블로그 인증을 확인합니다.
     * @param user 사용자
     * @param verificationCode 인증 코드
     * @return 연동 결과
     */
    public SnsConnectionResponse confirmVerification(User user, String verificationCode) {
        BlogVerificationData verificationData = getVerificationData(verificationCode, user.getId());
        
        boolean isVerified = verifyCodeInBlog(verificationData.getBlogUrl(), verificationCode);
        
        if (!isVerified) {
            throw new SnsException(ErrorMessage.SNS_CONNECTION_FAILED, "Blog verification failed");
        }
        
        SnsConnection connection = createOrUpdateBlogConnection(user, verificationData.getBlogUrl());
        verificationStore.remove(verificationCode);
        
        log.info("네이버 블로그 인증 완료: user={}, blogUrl={}", user.getId(), verificationData.getBlogUrl());
        
        return SnsConnectionResponse.from(connection);
    }

    /**
     * 블로그 URL을 검증합니다.
     * @param blogUrl 블로그 URL
     */
    private void validateBlogUrl(String blogUrl) {
        if (blogUrl == null || blogUrl.trim().isEmpty()) {
            throw new SnsException(ErrorMessage.INVALID_REQUEST, "Blog URL is empty");
        }
        
        if (!blogUrl.contains("blog.naver.com")) {
            throw new SnsException(ErrorMessage.INVALID_REQUEST, "Only Naver blog URLs are supported");
        }
        
        try {
            new java.net.URL(blogUrl);
        } catch (Exception e) {
            throw new SnsException(ErrorMessage.INVALID_REQUEST, "Invalid blog URL format");
        }
    }

    /**
     * 고유한 인증 코드를 생성합니다.
     * @return 인증 코드
     */
    private String generateUniqueCode() {
        return "체리단" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 인증 안내 메시지를 생성합니다.
     * @param verificationCode 인증 코드
     * @return 안내 메시지
     */
    private String createInstructions(String verificationCode) {
        return String.format(
            "다음 인증 코드를 블로그 소개글에 추가해주세요:\n\n" +
            "체리단인증코드: %s\n\n" +
            "추가 후 '인증 확인' 버튼을 눌러주세요.",
            verificationCode
        );
    }

    /**
     * 인증 데이터를 가져옵니다.
     * @param verificationCode 인증 코드
     * @param userId 사용자 ID
     * @return 인증 데이터
     */
    private BlogVerificationData getVerificationData(String verificationCode, Long userId) {
        BlogVerificationData verificationData = verificationStore.get(verificationCode);
        
        if (verificationData == null || !verificationData.getUserId().equals(userId)) {
            throw new SnsException(ErrorMessage.SNS_CONNECTION_FAILED, "Invalid verification code or user mismatch");
        }
        
        return verificationData;
    }

    /**
     * 블로그에서 인증 코드를 확인합니다.
     * @param blogUrl 블로그 URL
     * @param verificationCode 인증 코드
     * @return 인증 성공 여부
     */
    private boolean verifyCodeInBlog(String blogUrl, String verificationCode) {
        try {
            return blogVerificationClient.verifyCodeInBlog(blogUrl, verificationCode);
        } catch (Exception e) {
            log.error("블로그 인증 확인 실패: blogUrl={}, error={}", blogUrl, e.getMessage());
            return false;
        }
    }

    /**
     * 블로그 연동 정보를 생성하거나 업데이트합니다.
     * @param user 사용자
     * @param blogUrl 블로그 URL
     * @return SNS 연동 정보
     */
    private SnsConnection createOrUpdateBlogConnection(User user, String blogUrl) {
        SnsConnection connection = snsConnectionRepository.findByUserAndPlatformIgnoreActive(user, SnsPlatform.NAVER_BLOG)
                .orElseGet(() -> SnsConnection.builder().user(user).platform(SnsPlatform.NAVER_BLOG).build());
        
        connection.updateSnsInfo(
                blogUrl, // URL 자체를 snsUserId로 사용
                blogUrl,
                null, // accessToken 불필요
                null, // refreshToken 불필요
                LocalDateTime.now().plusYears(10) // 만료일을 10년 후로 설정
        );
        connection.setIsActive(true);
        
        return snsConnectionRepository.save(connection);
    }
} 