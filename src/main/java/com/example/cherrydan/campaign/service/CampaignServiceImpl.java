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
            campaigns = campaignRepository.findByCampaignType(type, pageable);
        } else {
            campaigns = campaignRepository.findAll(pageable);
        }
        
        return convertToResponseDTO(campaigns);
    }

    @Override
    public CampaignListResponseDTO getCampaignsBySnsPlatform(SnsPlatformType snsPlatformType, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        
        switch (snsPlatformType) {
            case BLOG:
                campaigns = campaignRepository.findByBlogTrue(pageable);
                break;
            case INSTAGRAM:
                campaigns = campaignRepository.findByInstagramTrue(pageable);
                break;
            case YOUTUBE:
                campaigns = campaignRepository.findByYoutubeTrue(pageable);
                break;
            case TIKTOK:
                campaigns = campaignRepository.findByTiktokTrue(pageable);
                break;
            case ETC:
                campaigns = campaignRepository.findByEtcTrue(pageable);
                break;
            case ALL:
            default:
                campaigns = campaignRepository.findAll(pageable);
                break;
        }
        
        return convertToResponseDTO(campaigns);
    }

    @Override
    public CampaignListResponseDTO getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        switch (campaignPlatformType) {
            case CHVU:
                campaigns = campaignRepository.findByExperiencePlatformCherivu(pageable);
                break;
            case REVU:
                campaigns = campaignRepository.findByExperiencePlatformRevu(pageable);
                break;
            case REVIEWNOTE:
                campaigns = campaignRepository.findByExperiencePlatformReviewnote(pageable);
                break;
            case DAILYVIEW:
                campaigns = campaignRepository.findByExperiencePlatformDailyview(pageable);
                break;
            case FOURBLOG:
                campaigns = campaignRepository.findByExperiencePlatformFourblog(pageable);
                break;
            case POPOMON:
                campaigns = campaignRepository.findByExperiencePlatformPopomon(pageable);
                break;
            case DINNERQUEEN:
                campaigns = campaignRepository.findByExperiencePlatformDinnerqueen(pageable);
                break;
            case SEOULOUBA:
                campaigns = campaignRepository.findByExperiencePlatformSeoulouba(pageable);
                break;
            case COMETOPLAY:
                campaigns = campaignRepository.findByExperiencePlatformCometoplay(pageable);
                break;
            case GANGNAM:
                campaigns = campaignRepository.findByExperiencePlatformGangnam(pageable);
                break;
            case ALL:
            default:
                campaigns = campaignRepository.findAll(pageable);
                break;
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
            .reviewerAnnouncementStatus(CampaignResponseDTO.getReviewerAnnouncementStatus(campaign.getReviewerAnnouncement()))
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