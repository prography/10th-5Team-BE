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

@Service
@RequiredArgsConstructor
public class CampaignSiteServiceImpl implements CampaignSiteService {
    private final CampaignSiteRepository campaignSiteRepository;

    @Value("${cdn.base-url}")
    private String cdnBaseUrl;

    @Override
    public List<CampaignSiteResponseDTO> getAllSites() {
        return campaignSiteRepository.findAllByIsActiveTrueOrderByPriorityAsc().stream()
                .map(site -> CampaignSiteResponseDTO.builder()
                        .siteNameKr(site.getSiteNameKr())
                        .cdnUrl(cdnBaseUrl.endsWith("/") ? cdnBaseUrl + site.getSiteNameEn() + ".png" : cdnBaseUrl + "/" + site.getSiteNameEn() + ".png")
                        .build())
                .collect(Collectors.toList());
    }
} 