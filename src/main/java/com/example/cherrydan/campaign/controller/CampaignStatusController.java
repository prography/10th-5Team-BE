package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusCountResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.cherrydan.campaign.service.CampaignStatusService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import com.example.cherrydan.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.example.cherrydan.common.exception.CampaignException;
import com.example.cherrydan.common.exception.ErrorMessage;

@Tag(name = "CampaignStatus", description = "내 체험단 상태 관리 및 팝업 API")
@RestController
@RequestMapping("/api/campaigns/my-status")
@RequiredArgsConstructor
public class CampaignStatusController {
    private final CampaignStatusService campaignStatusService;

    @Operation(summary = "내 체험단 상태별 목록 조회", description = "status 파라미터(APPLY/SELECTED/NOT_SELECTED/REVIEWING/ENDED) 기준 페이지네이션")
    @GetMapping
    public ResponseEntity<ApiResponse<PageListResponseDTO<CampaignStatusResponseDTO>>> getMyStatusesByType(
        @RequestParam(value = "status", defaultValue = "APPLY") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        CampaignStatusType statusType;
        try {
            statusType = CampaignStatusType.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CampaignException(ErrorMessage.CAMPAIGN_STATUS_INVALID);
        }
        Pageable pageable = PageRequest.of(page, size);
        PageListResponseDTO<CampaignStatusResponseDTO> result = campaignStatusService.getStatusesByType(currentUser.getId(), statusType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 카운트 조회", description = "상태별 개수를 반환합니다")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<CampaignStatusCountResponseDTO>> getMyStatusCounts(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        CampaignStatusCountResponseDTO result = campaignStatusService.getStatusCounts(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 생성/변경", description = "기존 데이터 있으면 is_active or status 변경, 없으면 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CampaignStatusResponseDTO>> createOrRecoverStatus(
        @Valid @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        CampaignStatusResponseDTO result = campaignStatusService.createOrRecoverStatus(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 변경", description = "is_active or status 변경")
    @PatchMapping
    public ResponseEntity<ApiResponse<CampaignStatusResponseDTO>> updateStatus(
        @Valid @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        CampaignStatusResponseDTO result = campaignStatusService.updateStatus(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 삭제", description = "campaignId만 받아서 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteStatus(
        @Valid @RequestBody DeleteRequest request,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        campaignStatusService.deleteStatus(request.getCampaignId(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "내 체험단 노출 팝업 조회", description = "지원한 공고/선정 결과/리뷰 작성 중 상태 중 기간이 지난 데이터만 최대 4개씩, 각 상태별 총 개수와 함께 반환")
    @GetMapping("/popup")
    public ResponseEntity<ApiResponse<CampaignStatusPopupResponseDTO>> getPopupStatus(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        CampaignStatusPopupResponseDTO result = campaignStatusService.getPopupStatusByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    public static class DeleteRequest {
        @NotNull(message = "캠페인 ID는 필수입니다.")
        private Long campaignId;

        public Long getCampaignId() { return campaignId; }
    }
} 