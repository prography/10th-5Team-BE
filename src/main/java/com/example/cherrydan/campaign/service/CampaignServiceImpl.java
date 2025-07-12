package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.common.aop.PerformanceMonitor;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.Collections;
import com.example.cherrydan.campaign.dto.CampaignResponseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public PageListResponseDTO<CampaignResponseDTO> getCampaigns(CampaignType type, String sort, Pageable pageable, Long userId) {
        Page<Campaign> campaigns;
        
        if (type != null) {
            campaigns = campaignRepository.findActiveByCampaignType(type, pageable);
        } else {
            campaigns = campaignRepository.findActiveCampaigns(pageable);
        }
        
        return convertToResponseDTO(campaigns, userId);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> getCampaignsBySnsPlatform(SnsPlatformType snsPlatformType, String sort, Pageable pageable, Long userId) {
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
        
        return convertToResponseDTO(campaigns, userId);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> getCampaignsByCampaignPlatform(CampaignPlatformType campaignPlatformType, String sort, Pageable pageable, Long userId) {
        if (campaignPlatformType == CampaignPlatformType.ALL) {
            return convertToResponseDTO(campaignRepository.findActiveCampaigns(pageable), userId);
        }
        
        Specification<Campaign> spec = (root, query, cb) -> cb.and(
                cb.isTrue(root.get("isActive")),
                cb.equal(root.get("sourceSite"), campaignPlatformType.getSourceSiteCode())
            );
        
        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
        return convertToResponseDTO(campaigns, userId);
    }

    @Override
    public PageListResponseDTO<CampaignResponseDTO> searchByKeyword(String keyword, Pageable pageable, Long userId) {
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
        return convertToResponseDTO(result, userId);
    }

    /**
     * 특정 키워드로 맞춤형 캠페인 목록 조회
     */
    @Override
    public Page<CampaignResponseDTO> getPersonalizedCampaignsByKeyword(Long userId, String keyword, Pageable pageable) {
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            String likeKeyword = "%" + keyword.trim() + "%";
            predicates.add(cb.or(
                cb.like(root.get("title"), likeKeyword),
                cb.like(root.get("address"), likeKeyword),
                cb.like(root.get("benefit"), likeKeyword)
            ));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
        return campaigns.map(campaign -> CampaignResponseDTO.fromEntityWithBookmark(campaign, false));
    }

    @Override
    @PerformanceMonitor
    public long getCampaignCountByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0;
        }
        String trimmedKeyword = keyword.trim();
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            String likeKeyword = "%" + trimmedKeyword + "%";
            predicates.add(cb.or(
                    cb.like(root.get("title"), likeKeyword),
                    cb.like(root.get("address"), likeKeyword),
                    cb.like(root.get("benefit"), likeKeyword)
            ));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return campaignRepository.count(spec);
    }

    private PageListResponseDTO<CampaignResponseDTO> convertToResponseDTO(Page<Campaign> campaigns, Long userId) {
        final Set<Long> bookmarkedCampaignIds = CampaignResponseMapper.getBookmarkedCampaignIds(bookmarkRepository, userId);
        List<CampaignResponseDTO> content = CampaignResponseMapper.toResponseDTOList(campaigns.getContent(), bookmarkedCampaignIds);
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