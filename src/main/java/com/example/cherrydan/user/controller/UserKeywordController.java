package com.example.cherrydan.user.controller;

import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.oauth.security.jwt.UserDetailsImpl;

import com.example.cherrydan.user.dto.UserKeywordResponseDTO;
import com.example.cherrydan.user.dto.KeywordCampaignAlertResponseDTO;
import com.example.cherrydan.user.service.UserKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        description = "사용자의 키워드 알림 히스토리를 조회합니다."
    )
    @GetMapping("/alerts")
    public List<KeywordCampaignAlertResponseDTO> getUserKeywordAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return userKeywordService.getUserKeywordAlerts(currentUser.getId());
    }
    

    @Operation(
        summary = "특정 키워드로 맞춤형 캠페인 조회",
        description = "특정 키워드로 매칭된 캠페인 목록을 조회합니다."
    )
    @GetMapping("/campaigns/personalized/keyword")
    public CampaignListResponseDTO getPersonalizedCampaignsByKeyword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return userKeywordService.getPersonalizedCampaignsByKeyword(currentUser.getId(), keyword, pageable);
    }

    @Operation(
        summary = "맞춤형 알림 개별 삭제",
        description = "특정 맞춤형 알림을 삭제합니다."
    )
    @DeleteMapping("/alerts/{alertId}")
    public void deleteKeywordAlert(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long alertId
    ) {
        userKeywordService.deleteKeywordAlert(currentUser.getId(), alertId);
    }
} 