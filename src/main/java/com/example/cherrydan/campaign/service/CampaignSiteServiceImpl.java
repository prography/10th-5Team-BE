package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.CampaignSite;
import com.example.cherrydan.campaign.dto.CampaignSiteResponseDTO;
import com.example.cherrydan.campaign.repository.CampaignSiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.example.cherrydan.common.util.CloudfrontUtil;

@Service
@RequiredArgsConstructor
public class CampaignSiteServiceImpl implements CampaignSiteService {
    private final CampaignSiteRepository campaignSiteRepository;

    @Value("${cdn.base-url}")
    private String cdnBaseUrl;

    @Override
    public List<CampaignSiteResponseDTO> getAllSites() {
        return campaignSiteRepository.findAllByIsActiveTrueOrderByPriorityAsc().stream()
            .map(site -> {
                String cdnUrl = CloudfrontUtil.getCampaignPlatformImageUrl(site.getSiteNameEn());
                return CampaignSiteResponseDTO.builder()
                        .siteNameKr(site.getSiteNameKr())
                        .siteNameEn(site.getSiteNameEn())
                        .cdnUrl(cdnUrl)
                        .build();
            })
            .collect(Collectors.toList());
    }
} 