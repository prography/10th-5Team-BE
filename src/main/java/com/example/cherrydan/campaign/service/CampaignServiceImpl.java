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
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

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

    @Override
    public CampaignListResponseDTO searchByKeyword(String keyword, Pageable pageable) {
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.or(
                cb.equal(root.get("campaignType"), CampaignType.REGION),
                cb.equal(root.get("campaignType"), CampaignType.PRODUCT)
            ));
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim() + "%";
                predicates.add(cb.like(root.get("title"), likeKeyword));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Campaign> result = campaignRepository.findAll(spec, pageable);
        return convertToResponseDTO(result);
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
        return CampaignResponseDTO.fromEntity(campaign);
    }
} 