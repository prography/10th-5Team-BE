package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.*;
import com.example.cherrydan.campaign.dto.CampaignFilterDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.dto.RegionSearchDTO;
import com.example.cherrydan.campaign.dto.SocialPlatformDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.common.exception.CampaignException;
import com.example.cherrydan.common.dto.PageResponseDTO;
import com.example.cherrydan.common.exception.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 캠페인 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CampaignService {

    private static final int MAX_KEYWORD_LENGTH = 15;
    
    private final CampaignRepository campaignRepository;

    /**
     * 지역과 카테고리로 캠페인 검색
     */
    public PageResponseDTO<CampaignResponseDTO> searchByRegionAndCategory(RegionSearchDTO searchDto, Pageable pageable) {
        log.info("지역별 캠페인 검색: region={}, category={}, page={}", 
                searchDto.getRegion(), searchDto.getRegionCategory(), pageable.getPageNumber());

        try {
            validateRegionSearch(searchDto);

            RegionParams regionParams = parseRegion(searchDto.getRegion());

            Page<Campaign> campaigns = campaignRepository.findByRegionAndCategory(
                    regionParams.mainRegion,
                    regionParams.detailRegion,
                    searchDto.getRegionCategory().name(), 
                    pageable);

            // 첫 번째 페이지에서 결과가 없으면 예외 던지기
            if (campaigns.isEmpty()) {
                throw new CampaignException(ErrorMessage.CAMPAIGN_SEARCH_NO_RESULTS);
            }

            log.info("검색 완료: 총 {}건", campaigns.getTotalElements());
            
            Page<CampaignResponseDTO> responsePage = campaigns.map(CampaignResponseDTO::from);
            return PageResponseDTO.from(responsePage);

        } catch (CampaignException e) {
            throw e;
        } catch (Exception e) {
            log.error("지역별 캠페인 검색 오류: {}", e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 키워드 + 필터로 캠페인 검색
     */
    public PageResponseDTO<CampaignResponseDTO> searchWithFilters(CampaignFilterDTO filter, Pageable pageable) {
        log.info("키워드+필터 검색: keyword='{}', page={}", filter.getKeyword(), pageable.getPageNumber());

        try {
            validateFilterSearch(filter);

            // 지역 처리 (공통 메서드 사용)
            RegionParams regionParams = parseRegion(filter.getRegion());

            SocialPlatformDTO sns = filter.getSns();

            Page<Campaign> campaigns = campaignRepository.findByKeywordAndFilters(
                    trimKeyword(filter.getKeyword()),
                    regionParams.mainRegion, regionParams.detailRegion,
                    filter.getRegionCategory(),
                    filter.getProduct(), filter.getReporter(),
                    sns != null ? sns.getYoutube() : 0,
                    sns != null ? sns.getShorts() : 0,
                    sns != null ? sns.getInsta() : 0,
                    sns != null ? sns.getReels() : 0,
                    sns != null ? sns.getBlog() : 0,
                    sns != null ? sns.getClip() : 0,
                    sns != null ? sns.getTiktok() : 0,
                    sns != null ? sns.getEtc() : 0,
                    getSearchStartDate(filter.getDeadlineStart()),
                    getSearchEndDate(filter.getDeadlineEnd()),
                    pageable);

            // 첫 번째 페이지에서 결과가 없으면 예외 던지기
            if (campaigns.isEmpty()) {
                throw new CampaignException(ErrorMessage.CAMPAIGN_SEARCH_NO_RESULTS);
            }

            log.info("검색 완료: 총 {}건", campaigns.getTotalElements());
            
            Page<CampaignResponseDTO> responsePage = campaigns.map(CampaignResponseDTO::from);
            return PageResponseDTO.from(responsePage);

        } catch (CampaignException e) {
            throw e;
        } catch (Exception e) {
            log.error("키워드+필터 검색 오류: {}", e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 캠페인 상세 조회
     */
    public CampaignResponseDTO getCampaign(Long id) {
        log.info("캠페인 상세 조회: id={}", id);
        
        try {
            validateCampaignId(id);
            
            Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));
            
            if (!campaign.isActive()) {
                throw new CampaignException(ErrorMessage.CAMPAIGN_INACTIVE);
            }
            
            return CampaignResponseDTO.from(campaign);

        } catch (CampaignException e) {
            throw e;
        } catch (Exception e) {
            log.error("캠페인 상세 조회 오류: id={}, error={}", id, e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 지역 검색 유효성 검증
     */
    private void validateRegionSearch(RegionSearchDTO searchDto) {
        if (searchDto == null || !searchDto.isValid()) {
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }

    /**
     * 필터 검색 유효성 검증
     */
    private void validateFilterSearch(CampaignFilterDTO filter) {
        if (filter == null) {
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }

        if (filter.hasKeywordFilter()) {
            String trimmedKeyword = trimKeyword(filter.getKeyword());
            if (trimmedKeyword.isEmpty()) {
                throw new CampaignException(ErrorMessage.CAMPAIGN_KEYWORD_EMPTY);
            }
            if (trimmedKeyword.length() > MAX_KEYWORD_LENGTH) {
                throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
            }
        }

        if (filter.isEmpty()) {
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }

    /**
     * 캠페인 ID 유효성 검증
     */
    private void validateCampaignId(Long id) {
        if (id == null || id <= 0) {
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }


    /**
     * 지역 파라미터 파싱 결과
     */
    private static class RegionParams {
        final Region mainRegion;
        final Region detailRegion;
        
        RegionParams(Region mainRegion, Region detailRegion) {
            this.mainRegion = mainRegion;
            this.detailRegion = detailRegion;
        }
    }

    /**
     * Region enum을 mainRegion과 detailRegion으로 구분
     */
    private RegionParams parseRegion(Region region) {
        if (region == null || region == Region.ALL) {
            return new RegionParams(null, null);
        }
        
        if (region.name().contains("_")) {
            return new RegionParams(null, region);  // detailRegion
        } else {
            return new RegionParams(region, null);  // mainRegion
        }
    }

    /**
     * 키워드 정리 (null-safe)
     */
    private String trimKeyword(String keyword) {
        return keyword != null ? keyword.trim() : "";
    }

    /**
     * 검색 시작 날짜 처리 (null이면 최소 날짜)
     */
    private LocalDate getSearchStartDate(LocalDate startDate) {
        return startDate != null ? startDate : LocalDate.of(1900, 1, 1);
    }

    /**
     * 검색 종료 날짜 처리 (null이면 최대 날짜)
     */
    private LocalDate getSearchEndDate(LocalDate endDate) {
        return endDate != null ? endDate : LocalDate.of(2100, 12, 31);
    }
}