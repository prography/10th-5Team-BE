package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignSiteResponseDTO;
import com.example.cherrydan.campaign.service.CampaignSiteService;
import com.example.cherrydan.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/campaign/site")
@RequiredArgsConstructor
public class CampaignSiteController {
    private final CampaignSiteService campaignSiteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignSiteResponseDTO>>> getAllSites() {
        List<CampaignSiteResponseDTO> sites = campaignSiteService.getAllSites();
        return ResponseEntity.ok(ApiResponse.success("캠페인 사이트 목록 조회 성공", sites));
    }
} 