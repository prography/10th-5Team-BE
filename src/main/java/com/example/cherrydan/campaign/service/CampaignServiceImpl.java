package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
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
    public PageListResponseDTO<CampaignResponseDTO> getCampaigns(CampaignType type, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        
        if (type != null) {
            campaigns = campaignRepository.findActiveByCampaignType(type, pageable);
        } else {
            campaigns = campaignRepository.findActiveCampaigns(pageable);
        }
        
        return convertToResponseDTO(campaigns);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> getCampaignsBySnsPlatform(SnsPlatformType snsPlatformType, String sort, Pageable pageable) {
        Page<Campaign> campaigns;
        
        switch (snsPlatformType) {
            case BLOG:
                campaigns = campaignRepository.findActiveByBlogTrue(pageable);
                break;
            case INSTAGRAM:
                campaigns = campaignRepository.findActiveByInstagramTrue(pageable);
                break;
            case YOUTUBE:
                campaigns = campaignRepository.findActiveByYoutubeTrue(pageable);
                break;
            case TIKTOK:
                campaigns = campaignRepository.findActiveByTiktokTrue(pageable);
                break;
            case ETC:
                campaigns = campaignRepository.findActiveByEtcTrue(pageable);
                break;
            case ALL:
            default:
                campaigns = campaignRepository.findActiveCampaigns(pageable);
                break;
        }
        
        return convertToResponseDTO(campaigns);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable) {
        if (campaignPlatformType == CampaignPlatformType.ALL) {
            return convertToResponseDTO(campaignRepository.findActiveCampaigns(pageable));
        }
        
        Specification<Campaign> spec = (root, query, cb) -> cb.and(
                cb.isTrue(root.get("isActive")),
                cb.equal(root.get("sourceSite"), campaignPlatformType.getSourceSiteCode())
            );
        
        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
        return convertToResponseDTO(campaigns);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
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

    private PageListResponseDTO<CampaignResponseDTO> convertToResponseDTO(Page<Campaign> campaigns) {
        List<CampaignResponseDTO> content = campaigns.getContent().stream()
            .map(CampaignResponseDTO::fromEntity)
            .collect(Collectors.toList());
        
        return PageListResponseDTO.<CampaignResponseDTO>builder()
            .content(content)
            .page(campaigns.getNumber())
            .size(campaigns.getSize())
            .totalElements(campaigns.getTotalElements())
            .totalPages(campaigns.getTotalPages())
            .hasNext(campaigns.hasNext())
            .hasPrevious(campaigns.hasPrevious())
            .build();
    }
} 