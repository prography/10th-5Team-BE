package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;
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

@Tag(name = "CampaignStatus", description = "내 체험단 상태 관리 및 팝업 API")
@RestController
@RequestMapping("/api/campaigns/my-status")
@RequiredArgsConstructor
public class CampaignStatusController {
    private final CampaignStatusService campaignStatusService;

    @Operation(summary = "내 체험단 상태 전체 조회", description = "userId 기준 전체 상태 조회(상태별 리스트+카운트)")
    @GetMapping
    public ResponseEntity<ApiResponse<CampaignStatusListResponseDTO>> getMyStatusListWithCount(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Long userId = currentUser.getId();
        CampaignStatusListResponseDTO result = campaignStatusService.getStatusListWithCountByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 생성/변경", description = "기존 데이터 있으면 is_active or status 변경, 없으면 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CampaignStatusResponseDTO>> createOrRecoverStatus(
        @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        CampaignStatusResponseDTO result = campaignStatusService.createOrRecoverStatus(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 변경", description = "is_active or status 변경")
    @PatchMapping
    public ResponseEntity<ApiResponse<CampaignStatusResponseDTO>> updateStatus(
        @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        CampaignStatusResponseDTO result = campaignStatusService.updateStatus(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "내 체험단 상태 삭제", description = "campaignId만 받아서 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteStatus(
        @RequestBody DeleteRequest request,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        campaignStatusService.deleteStatus(request.getCampaignId(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "내 체험단 노출 팝업 조회", description = "신청/선정/등록 상태 중 기간이 지난 데이터만 최대 4개씩, 각 상태별 총 개수와 함께 반환")
    @GetMapping("/popup")
    public ResponseEntity<ApiResponse<CampaignStatusPopupResponseDTO>> getPopupStatus(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        CampaignStatusPopupResponseDTO result = campaignStatusService.getPopupStatusByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    public static class DeleteRequest {
        private Long campaignId;
        public Long getCampaignId() { return campaignId; }
        public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    }
} 