package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.dto.CampaignListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.domain.RegionGroup;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.CampaignException;
import com.example.cherrydan.common.util.StringUtil;
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

import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.campaign.domain.LocalCategory;
import com.example.cherrydan.campaign.domain.ProductCategory;

@Service
@RequiredArgsConstructor
public class CampaignCategoryServiceImpl implements CampaignCategoryService {

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignListResponseDTO searchByCategory(String regionGroup, String subRegion, String local, String product, String reporter, String snsPlatform, String experiencePlatform, Pageable pageable) {
        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));

            // regionGroup, subRegion 조건 처리
            boolean hasRegionGroup = regionGroup != null && !regionGroup.isEmpty();
            boolean hasSubRegion = subRegion != null && !subRegion.isEmpty();

            if (hasRegionGroup) {
                RegionGroup regionGroupEnum;
                try {
                    regionGroupEnum = RegionGroup.fromCodeName(regionGroup);
                } catch (IllegalArgumentException e) {
                    throw new CampaignException(ErrorMessage.CAMPAIGN_REGION_GROUP_NOT_FOUND);
                }
                predicates.add(cb.equal(root.get("regionGroup"), regionGroupEnum.getCode()));
            }
            if (hasSubRegion) {
                RegionGroup.RegionGroupSubRegionMatch match = RegionGroup.findBySubRegionCodeName(subRegion)
                        .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_REGION_DETAIL_NOT_FOUND));
                predicates.add(cb.equal(root.get("regionDetail"), match.getSubRegion().getCode()));
            }

            List<Predicate> typePredicates = new ArrayList<>();

            // 지역 카테고리 조건 처리
            if (local != null && !local.isEmpty() && !local.equalsIgnoreCase("all")) {
                try {
                    int code = LocalCategory.fromString(local).getCode();
                    typePredicates.add(cb.and(
                        cb.equal(root.get("campaignType"), CampaignType.REGION),
                        cb.equal(root.get("localCategory"), code)
                    ));
                } catch (IllegalArgumentException e) {
                    throw new CampaignException(ErrorMessage.CAMPAIGN_REGION_DETAIL_NOT_FOUND);
                }
            }

            // 제품 카테고리 조건 처리
            if (product != null && !product.isEmpty() && !product.equalsIgnoreCase("all")) {
                try {
                    int code = ProductCategory.fromString(product).getCode();
                    typePredicates.add(cb.and(
                        cb.equal(root.get("campaignType"), CampaignType.PRODUCT),
                        cb.equal(root.get("productCategory"), code)
                    ));
                } catch (IllegalArgumentException e) {
                    throw new CampaignException(ErrorMessage.CAMPAIGN_PRODUCT_CATEGORY_NOT_FOUND);
                }
            }

            // 기자단 카테고리 조건 처리
            if (reporter != null && !reporter.isEmpty()) {
                typePredicates.add(cb.equal(root.get("campaignType"), CampaignType.REPORTER));
            }

            // typePredicates가 비어있지 않으면 OR로 묶어서 predicates에 추가
            if (!typePredicates.isEmpty()) {
                predicates.add(cb.or(typePredicates.toArray(new Predicate[0])));
            }

            // SNS 플랫폼 조건 처리
            String snsPlatformNorm = StringUtil.normalize(snsPlatform);
            if (snsPlatformNorm != null && !snsPlatformNorm.isEmpty() && !snsPlatformNorm.equals("all")) {
                switch (snsPlatformNorm) {
                    case "blog":
                        predicates.add(cb.or(
                            cb.isTrue(root.get("blog")),
                            cb.isTrue(root.get("clip"))
                        ));
                        break;
                    case "youtube":
                        predicates.add(cb.or(
                            cb.isTrue(root.get("youtube")),
                            cb.isTrue(root.get("shorts"))
                        ));
                        break;
                    case "insta":
                        predicates.add(cb.or(
                            cb.isTrue(root.get("insta")),
                            cb.isTrue(root.get("reels"))
                        ));
                        break;
                    case "tiktok":
                        predicates.add(cb.isTrue(root.get("tiktok")));
                        break;
                    case "etc":
                        predicates.add(cb.isTrue(root.get("etc")));
                        break;
                }
            }

            // 체험단 플랫폼 조건 처리
            String experiencePlatformNorm = StringUtil.normalize(experiencePlatform);
            if (experiencePlatformNorm != null && !experiencePlatformNorm.isEmpty()) {
                predicates.add(cb.equal(root.get("sourceSite"), experiencePlatformNorm));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaigns = campaignRepository.findAll(spec, pageable);
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
        return com.example.cherrydan.campaign.dto.CampaignResponseDTO.fromEntity(campaign);
    }
} 