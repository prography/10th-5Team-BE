package com.example.cherrydan.campaign.controller;

import com.example.cherrydan.campaign.dto.CampaignSiteResponseDTO;
import com.example.cherrydan.campaign.service.CampaignSiteService;
import com.example.cherrydan.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Campaign API", description = "캠페인(체험단) 관련 API")
@RestController
@RequestMapping("/api/campaigns/site")
@RequiredArgsConstructor
public class CampaignSiteController {
    private final CampaignSiteService campaignSiteService;

    @Operation(
        summary = "캠페인 사이트 목록 조회",
        description = "캠페인 사이트의 한글명과 CDN URL 목록을 우선순위대로 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignSiteResponseDTO>>> getAllSites() {
        List<CampaignSiteResponseDTO> sites = campaignSiteService.getAllSites();
        return ResponseEntity.ok(ApiResponse.success("캠페인 사이트 목록 조회 성공", sites));
    }
} 