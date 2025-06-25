package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignListResponseDTO getCampaigns(CampaignType type, String region, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        
        if (type != null) {
            System.out.println("Executing filtered query for type: " + type + " with ordinal: " + type.ordinal());
            campaigns = campaignRepository.findByCampaignType(type, pageable);
        } else {
            System.out.println("Executing query for all campaigns (no type filter)");
            campaigns = campaignRepository.findAll(pageable);
        }
        
        return convertToResponseDTO(campaigns);
    }

    private CampaignListResponseDTO convertToResponseDTO(Page<Campaign> campaigns) {
        List<CampaignResponseDTO> content = campaigns.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return CampaignListResponseDTO.builder()
            .content(content)
            .page(campaigns.getNumber())
            .size(campaigns.getSize())
            .totalElements(campaigns.getTotalElements())
            .totalPages(campaigns.getTotalPages())
            .hasNext(campaigns.hasNext())
            .hasPrevious(campaigns.hasPrevious())
            .build();
    }

    private CampaignResponseDTO toDTO(Campaign campaign) {
        return CampaignResponseDTO.builder()
            .id(campaign.getId())
            .title(campaign.getTitle())
            .detailUrl(campaign.getDetailUrl())
            .benefit(campaign.getBenefit())
            .applyStart(campaign.getApplyStart())
            .applyEnd(campaign.getApplyEnd())
            .reviewerAnnouncement(campaign.getReviewerAnnouncement())
            .contentSubmissionStart(campaign.getContentSubmissionStart())
            .contentSubmissionEnd(campaign.getContentSubmissionEnd())
            .resultAnnouncement(campaign.getResultAnnouncement())
            .applicantCount(campaign.getApplicantCount())
            .recruitCount(campaign.getRecruitCount())
            .sourceSite(campaign.getSourceSite())
            .imageUrl(campaign.getImageUrl())
            .youtube(campaign.getYoutube())
            .shorts(campaign.getShorts())
            .insta(campaign.getInsta())
            .reels(campaign.getReels())
            .blog(campaign.getBlog())
            .clip(campaign.getClip())
            .tiktok(campaign.getTiktok())
            .etc(campaign.getEtc())
            .campaignType(campaign.getCampaignType())
            .address(campaign.getAddress())
            .competitionRate(campaign.getCompetitionRate())
            .localCategory(campaign.getLocalCategory())
            .productCategory(campaign.getProductCategory())
            .build();
    }
} 