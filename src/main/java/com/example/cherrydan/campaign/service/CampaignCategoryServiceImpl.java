package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.domain.RegionGroup;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.CampaignException;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.LocalCategory;
import com.example.cherrydan.campaign.domain.ProductCategory;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.common.response.PageListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseMapper;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
import com.example.cherrydan.campaign.domain.Bookmark;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CampaignCategoryServiceImpl implements CampaignCategoryService {

    private final CampaignRepository campaignRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public PageListResponseDTO<CampaignResponseDTO> searchByCategory(String title, List<String> regionGroup, List<String> subRegion, List<String> local, List<String> product, String reporter, List<String> snsPlatform, List<String> campaignPlatform, String applyStart, String applyEnd, Pageable pageable, Long userId) {
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isActive"), true));

            // 제목 조건 처리
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + title.trim() + "%"));
            }

            // regionGroup 조건 처리 (복수 선택 가능)
            if (regionGroup != null && !regionGroup.isEmpty() && !regionGroup.contains("all")) {
                List<Predicate> regionGroupPredicates = new ArrayList<>();
                for (String regionGroupItem : regionGroup) {
                    if (regionGroupItem != null && !regionGroupItem.isEmpty()) {
                        try {
                            RegionGroup regionGroupEnum = RegionGroup.fromCodeName(regionGroupItem);
                            regionGroupPredicates.add(cb.equal(root.get("regionGroup"), regionGroupEnum.getCode()));
                        } catch (IllegalArgumentException e) {
                            throw new CampaignException(ErrorMessage.CAMPAIGN_REGION_GROUP_NOT_FOUND);
                        }
                    }
                }
                if (!regionGroupPredicates.isEmpty()) {
                    predicates.add(cb.or(regionGroupPredicates.toArray(new Predicate[0])));
                }
            }

            // subRegion 조건 처리 (복수 선택 가능)
            if (subRegion != null && !subRegion.isEmpty()) {
                List<Predicate> subRegionPredicates = new ArrayList<>();
                for (String subRegionItem : subRegion) {
                    if (subRegionItem != null && !subRegionItem.isEmpty()) {
                        try {
                            RegionGroup.RegionGroupSubRegionMatch match = RegionGroup.findBySubRegionCodeName(subRegionItem)
                                    .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_REGION_DETAIL_NOT_FOUND));
                            subRegionPredicates.add(cb.equal(root.get("regionDetail"), match.getSubRegion().getCode()));
                        } catch (IllegalArgumentException e) {
                            throw new CampaignException(ErrorMessage.CAMPAIGN_REGION_DETAIL_NOT_FOUND);
                        }
                    }
                }
                if (!subRegionPredicates.isEmpty()) {
                    predicates.add(cb.or(subRegionPredicates.toArray(new Predicate[0])));
                }
            }

            List<Predicate> typePredicates = new ArrayList<>();

            // 지역 카테고리 조건 처리 (복수 선택 가능)
            if (local != null && !local.isEmpty()) {
                if (local.size() == 1 && local.get(0).equalsIgnoreCase("all")) {
                    // all만 있으면 campaign_type=1만 추가
                    typePredicates.add(cb.equal(root.get("campaignType"), CampaignType.REGION));
                } else {
                    List<Predicate> localPredicates = new ArrayList<>();
                    for (String localItem : local) {
                        if (localItem != null && !localItem.isEmpty() && !localItem.equalsIgnoreCase("all")) {
                            try {
                                int code = LocalCategory.fromString(localItem).getCode();
                                localPredicates.add(cb.and(
                                    cb.equal(root.get("campaignType"), CampaignType.REGION),
                                    cb.equal(root.get("localCategory"), code)
                                ));
                            } catch (IllegalArgumentException e) {
                                throw new CampaignException(ErrorMessage.CAMPAIGN_REGION_DETAIL_NOT_FOUND);
                            }
                        }
                    }
                    if (!localPredicates.isEmpty()) {
                        typePredicates.add(cb.or(localPredicates.toArray(new Predicate[0])));
                    }
                }
            }

            // 제품 카테고리 조건 처리 (복수 선택 가능)
            if (product != null && !product.isEmpty()) {
                if (product.size() == 1 && product.get(0).equalsIgnoreCase("all")) {
                    // all만 있으면 campaign_type=2만 추가
                    typePredicates.add(cb.equal(root.get("campaignType"), CampaignType.PRODUCT));
                } else {
                    List<Predicate> productPredicates = new ArrayList<>();
                    for (String productItem : product) {
                        if (productItem != null && !productItem.isEmpty() && !productItem.equalsIgnoreCase("all")) {
                            try {
                                int code = ProductCategory.fromString(productItem).getCode();
                                productPredicates.add(cb.and(
                                    cb.equal(root.get("campaignType"), CampaignType.PRODUCT),
                                    cb.equal(root.get("productCategory"), code)
                                ));
                            } catch (IllegalArgumentException e) {
                                throw new CampaignException(ErrorMessage.CAMPAIGN_PRODUCT_CATEGORY_NOT_FOUND);
                            }
                        }
                    }
                    if (!productPredicates.isEmpty()) {
                        typePredicates.add(cb.or(productPredicates.toArray(new Predicate[0])));
                    }
                }
            }

            // 기자단 카테고리 조건 처리
            if (reporter != null && reporter.equalsIgnoreCase("all")) {
                typePredicates.add(cb.equal(root.get("campaignType"), CampaignType.REPORTER));
            }            

            // typePredicates가 비어있지 않으면 OR로 묶어서 predicates에 추가
            if (!typePredicates.isEmpty()) {
                predicates.add(cb.or(typePredicates.toArray(new Predicate[0])));
            }

            // SNS 플랫폼 조건 처리 (복수 선택 가능)
            if (snsPlatform != null && !snsPlatform.isEmpty() && !snsPlatform.contains("all")) {
                List<Predicate> snsPlatformPredicates = new ArrayList<>();
                for (String snsPlatformItem : snsPlatform) {
                    if (snsPlatformItem != null && !snsPlatformItem.trim().isEmpty() && !snsPlatformItem.trim().equalsIgnoreCase("all")) {
                        try {
                            SnsPlatformType snsPlatformType = SnsPlatformType.fromCode(snsPlatformItem);
                            String[] relatedFields = snsPlatformType.getRelatedFields();
                            
                            if (relatedFields.length == 1) {
                                snsPlatformPredicates.add(cb.isTrue(root.get(relatedFields[0])));
                            } else if (relatedFields.length > 1) {
                                List<Predicate> platformPredicates = new ArrayList<>();
                                for (String field : relatedFields) {
                                    platformPredicates.add(cb.isTrue(root.get(field)));
                                }
                                snsPlatformPredicates.add(cb.or(platformPredicates.toArray(new Predicate[0])));
                            }
                        } catch (IllegalArgumentException e) {
                            throw new CampaignException(ErrorMessage.CAMPAIGN_SNS_NOT_FOUND);
                        }
                    }
                }
                if (!snsPlatformPredicates.isEmpty()) {
                    predicates.add(cb.or(snsPlatformPredicates.toArray(new Predicate[0])));
                }
            }

            // 캠페인 플랫폼 조건 처리 (복수 선택 가능)
            if (campaignPlatform != null && !campaignPlatform.isEmpty() && !campaignPlatform.contains("all")) {
                List<Predicate> campaignPlatformPredicates = new ArrayList<>();
                for (String campaignPlatformItem : campaignPlatform) {
                    if (campaignPlatformItem != null && !campaignPlatformItem.trim().isEmpty()) {
                        campaignPlatformPredicates.add(cb.equal(root.get("sourceSite"), campaignPlatformItem.trim()));
                    }
                }
                if (!campaignPlatformPredicates.isEmpty()) {
                    predicates.add(cb.or(campaignPlatformPredicates.toArray(new Predicate[0])));
                }
            }

            // 마감일 조건 처리
            if (applyStart != null && !applyStart.trim().isEmpty()) {
                try {
                    LocalDate startDate = LocalDate.parse(applyStart.trim());
                    predicates.add(cb.greaterThanOrEqualTo(root.get("applyEnd"), startDate));
                } catch (DateTimeParseException e) {
                    throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
                }
            }
            
            if (applyEnd != null && !applyEnd.trim().isEmpty()) {
                try {
                    LocalDate endDate = LocalDate.parse(applyEnd.trim());
                    predicates.add(cb.lessThanOrEqualTo(root.get("applyEnd"), endDate));
                } catch (DateTimeParseException e) {
                    throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
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