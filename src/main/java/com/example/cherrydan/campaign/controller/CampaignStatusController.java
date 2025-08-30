package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusCountResponseDTO;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.common.response.EmptyResponse;
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
import com.example.cherrydan.campaign.dto.CampaignStatusBatchRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusDeleteRequestDTO;

import java.util.List;

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
        CampaignStatusResponseDTO result = campaignStatusService.createOrRecoverStatus(requestDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 변경", description = "배치로 여러 캠페인 상태 변경")
    @PatchMapping
    public ResponseEntity<ApiResponse<List<CampaignStatusResponseDTO>>> updateStatus(
        @Valid @RequestBody CampaignStatusBatchRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        List<CampaignStatusResponseDTO> results = campaignStatusService.updateStatusBatch(requestDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "내 체험단 상태 삭제", description = "campaignIds 리스트로 일괄 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<EmptyResponse>> deleteStatus(
        @Valid @RequestBody CampaignStatusDeleteRequestDTO request,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        campaignStatusService.deleteStatusBatch(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("체험단 상태 삭제 성공"));
    }

    @Operation(summary = "내 체험단 노출 팝업 조회", description = "지원한 공고/선정 결과/리뷰 작성 중 상태 중 기간이 지난 데이터만 최대 4개씩, 각 상태별 총 개수와 함께 반환")
    @GetMapping("/popup")
    public ResponseEntity<ApiResponse<CampaignStatusPopupResponseDTO>> getPopupStatus(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        CampaignStatusPopupResponseDTO result = campaignStatusService.getPopupStatusByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
} 