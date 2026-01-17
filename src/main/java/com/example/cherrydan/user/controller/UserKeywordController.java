package com.example.cherrydan.user.controller;

import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.common.exception.AuthException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.response.ApiResponse;
import com.example.cherrydan.common.response.EmptyResponse;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;

import com.example.cherrydan.user.dto.UserKeywordRequestDTO;
import com.example.cherrydan.user.dto.UserKeywordResponseDTO;
import com.example.cherrydan.user.dto.KeywordCampaignAlertResponseDTO;
import com.example.cherrydan.user.dto.AlertIdsRequestDTO;
import com.example.cherrydan.user.service.UserKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Keyword Campaign", description = "키워드 맞춤형 캠페인 관련 API")
@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class UserKeywordController {
    
    private final UserKeywordService userKeywordService;

    @Operation(
        summary = "내 키워드 알림 목록 조회",
        description = """
            사용자의 키워드 알림 히스토리를 조회합니다.

            **쿼리 파라미터 예시:**
            - ?page=0&size=20&sort=alertDate,desc
            - ?page=1&size=10&sort=alertDate,asc

            **정렬 가능한 필드:**
            - alertDate: 알림 발송 날짜 (기본값, DESC)

            **여러 정렬 조건 (쿼리 파라미터):**
            - ?sort=alertDate,desc&sort=id,asc (복수 정렬)
            - ?sort=alertDate,desc (단일 정렬, 기본값)
            - ?sort=alertDate,asc (오래된 순)

            sort : 정렬 기준 (예: alertDate,desc) -> 선택사항

            **주의:** 이는 Request Body가 아닌 **Query Parameter**입니다.
            """,
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<PageListResponseDTO<KeywordCampaignAlertResponseDTO>>> getUserKeywordAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PageableDefault(size = 20, sort = "alertDate") Pageable pageable
    ) {
        Page<KeywordCampaignAlertResponseDTO> alerts = userKeywordService.getUserKeywordAlerts(currentUser.getId(), pageable);
        PageListResponseDTO<KeywordCampaignAlertResponseDTO> response = PageListResponseDTO.from(alerts);
        return ResponseEntity.ok(ApiResponse.success("키워드 알림 목록 조회 성공", response));
    }


    @Operation(
        summary = "내 키워드 목록 조회",
        description = "사용자가 등록한 키워드 목록을 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<UserKeywordResponseDTO>>> getMyKeywords(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        List<UserKeywordResponseDTO> keywords = userKeywordService.getKeywords(currentUser.getId())
            .stream().map(UserKeywordResponseDTO::fromKeyword).toList();
        return ResponseEntity.ok(ApiResponse.success("키워드 목록 조회 성공", keywords));
    }

    @Operation(
        summary = "내 키워드 등록",
        description = "새로운 키워드를 등록합니다. 이미 등록된 키워드인 경우 400 에러를 반환합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<EmptyResponse>> addMyKeyword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody UserKeywordRequestDTO request
    ) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        userKeywordService.addKeyword(currentUser.getId(), request.getKeyword());
        return ResponseEntity.ok(ApiResponse.success("키워드 등록 성공"));
    }

    @Operation(
        summary = "내 키워드 삭제",
        description = "등록된 키워드를 삭제합니다. 본인의 키워드가 아닌 경우 404 에러를 반환합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/me/{keywordId}")
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteMyKeyword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable("keywordId") Long keywordId
    ) {
        if (currentUser == null) throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        userKeywordService.removeKeywordById(currentUser.getId(), keywordId);
        return ResponseEntity.ok(ApiResponse.success("키워드 삭제 성공"));
    }

    @Operation(
        summary = "특정 키워드로 맞춤형 캠페인 조회",
        description = """
            특정 키워드로 매칭된 캠페인 목록을 조회합니다.
            등록되지 않은 키워드로 조회 시 404 에러를 반환합니다.

            **쿼리 파라미터 예시:**
            - ?page=0&size=20&keyword=고기
            - ?page=1&size=10&keyword=서울

            page : 페이지 번호

            size : 페이지 크기 (한 페이지당 캠페인 수)

            sort : 정렬 기준 (예: reviewerAnnouncement,desc) -> 선택사항

            **주의:** 이는 Request Body가 아닌 **Query Parameter**입니다.
            """,
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/campaigns/personalized")
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignResponseDTO>>> getPersonalizedCampaignsByKeyword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("keyword") String keyword,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CampaignResponseDTO> campaigns = userKeywordService.getPersonalizedCampaignsByKeyword(keyword, date, currentUser.getId(), pageable);
        PageListResponseDTO<CampaignResponseDTO> response = PageListResponseDTO.from(campaigns);
        return ResponseEntity.ok(ApiResponse.success("맞춤형 캠페인 조회 성공", response));
    }

    @Operation(
        summary = "맞춤형 알림 삭제",
        description = "선택한 맞춤형 알림들을 삭제합니다. 본인의 알림이 아닌 경우 403 에러를 반환합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/alerts")
    public ResponseEntity<ApiResponse<Void>> deleteKeywordAlert(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody AlertIdsRequestDTO request
    ) {
        userKeywordService.deleteKeywordAlert(currentUser.getId(), request.getAlertIds());
        return ResponseEntity.ok(ApiResponse.success("키워드 알림 삭제 성공", null));
    }

    @Operation(
        summary = "키워드 알림 읽음 처리",
        description = "선택한 키워드 알림들을 읽음 상태로 변경합니다. 본인의 알림이 아닌 경우 403 에러를 반환합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PatchMapping("/alerts/read")
    public ResponseEntity<ApiResponse<Void>> markKeywordAlertsAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody AlertIdsRequestDTO request
    ) {
        userKeywordService.markKeywordAlertsAsRead(currentUser.getId(), request.getAlertIds());
        return ResponseEntity.ok(ApiResponse.success("키워드 알림 읽음 처리 성공", null));
    }
} 