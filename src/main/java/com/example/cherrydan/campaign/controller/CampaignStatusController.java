package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.service.CampaignStatusService;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns/my-status")
@RequiredArgsConstructor
public class CampaignStatusController {
    private final CampaignStatusService campaignStatusService;

    @GetMapping
    public ResponseEntity<CampaignStatusListResponseDTO> getMyStatusListWithCount(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Long userId = currentUser.getId();
        return ResponseEntity.ok(campaignStatusService.getStatusListWithCountByUser(userId));
    }

    @PostMapping
    public ResponseEntity<CampaignStatusResponseDTO> createOrRecoverStatus(
        @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        return ResponseEntity.ok(campaignStatusService.createOrRecoverStatus(requestDTO));
    }

    @PatchMapping
    public ResponseEntity<CampaignStatusResponseDTO> updateStatus(
        @RequestBody CampaignStatusRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        requestDTO.setUserId(currentUser.getId());
        return ResponseEntity.ok(campaignStatusService.updateStatus(requestDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteStatus(
        @RequestBody DeleteRequest request,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        campaignStatusService.deleteStatus(request.getCampaignId(), currentUser.getId());
        return ResponseEntity.ok().build();
    }

    public static class DeleteRequest {
        private Long campaignId;
        public Long getCampaignId() { return campaignId; }
        public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    }
} 