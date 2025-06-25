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

    @Override
    public CampaignListResponseDTO getCampaignsBySnsPlatform(SnsPlatformType snsPlatformType, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        
        switch (snsPlatformType) {
            case BLOG:
                System.out.println("Executing query for blog campaigns");
                campaigns = campaignRepository.findByBlogTrue(pageable);
                break;
            case INSTAGRAM:
                System.out.println("Executing query for instagram campaigns (insta OR reels)");
                campaigns = campaignRepository.findByInstagramTrue(pageable);
                break;
            case YOUTUBE:
                System.out.println("Executing query for youtube campaigns (youtube OR shorts)");
                campaigns = campaignRepository.findByYoutubeTrue(pageable);
                break;
            case TIKTOK:
                System.out.println("Executing query for tiktok campaigns");
                campaigns = campaignRepository.findByTiktokTrue(pageable);
                break;
            case ETC:
                System.out.println("Executing query for etc campaigns");
                campaigns = campaignRepository.findByEtcTrue(pageable);
                break;
            case ALL:
            default:
                System.out.println("Executing query for all campaigns (no platform filter)");
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
                System.out.println("Executing query for cherivu campaigns");
                campaigns = campaignRepository.findByExperiencePlatformCherivu(pageable);
                break;
            case REVU:
                System.out.println("Executing query for revu campaigns");
                campaigns = campaignRepository.findByExperiencePlatformRevu(pageable);
                break;
            case REVIEWNOTE:
                System.out.println("Executing query for reviewnote campaigns");
                campaigns = campaignRepository.findByExperiencePlatformReviewnote(pageable);
                break;
            case DAILYVIEW:
                System.out.println("Executing query for dailyview campaigns");
                campaigns = campaignRepository.findByExperiencePlatformDailyview(pageable);
                break;
            case FOURBLOG:
                System.out.println("Executing query for 4blog campaigns");
                campaigns = campaignRepository.findByExperiencePlatformFourblog(pageable);
                break;
            case POPOMON:
                System.out.println("Executing query for popomon campaigns");
                campaigns = campaignRepository.findByExperiencePlatformPopomon(pageable);
                break;
            case DINNERQUEEN:
                System.out.println("Executing query for dinnerqueen campaigns");
                campaigns = campaignRepository.findByExperiencePlatformDinnerqueen(pageable);
                break;
            case SEOULOUBA:
                System.out.println("Executing query for seoulouba campaigns");
                campaigns = campaignRepository.findByExperiencePlatformSeoulouba(pageable);
                break;
            case COMETOPLAY:
                System.out.println("Executing query for cometoplay campaigns");
                campaigns = campaignRepository.findByExperiencePlatformCometoplay(pageable);
                break;
            case GANGNAM:
                System.out.println("Executing query for gangnam campaigns");
                campaigns = campaignRepository.findByExperiencePlatformGangnam(pageable);
                break;
            case ALL:
            default:
                System.out.println("Executing query for all campaigns (no campaign platform filter)");
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