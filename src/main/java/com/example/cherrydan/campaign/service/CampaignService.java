package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.*;
import com.example.cherrydan.campaign.dto.CampaignFilterDTO;
import com.example.cherrydan.campaign.dto.CampaignResponseDTO;
import com.example.cherrydan.campaign.dto.RegionSearchDTO;
import com.example.cherrydan.campaign.dto.SocialPlatformDTO;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.common.exception.CampaignException;
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

    private static final int MAX_KEYWORD_LENGTH = 100;
    
    private final CampaignRepository campaignRepository;

    /**
     * 지역과 카테고리로 캠페인 검색
     */
    public Page<CampaignResponseDTO> searchByRegionAndCategory(RegionSearchDTO searchDto, Pageable pageable) {
        log.info("지역별 캠페인 검색 시작: region={}, category={}", 
                searchDto.getRegion(), searchDto.getRegionCategory());

        try {
            // 유효성 검증
            validateRegionSearch(searchDto);

            // 지역과 카테고리 조합 검증
            validateRegionCategoryCombo(searchDto.getRegion(), searchDto.getRegionCategory());

            // 검색 실행
            Page<Campaign> campaigns = campaignRepository.findByRegionAndCategory(
                    searchDto.getRegion().name(), 
                    searchDto.getRegionCategory().name(), 
                    pageable);

            log.info("지역별 캠페인 검색 완료: 총 {}건", campaigns.getTotalElements());
            return campaigns.map(CampaignResponseDTO::from);

        } catch (CampaignException e) {
            log.error("지역별 캠페인 검색 실패 - 비즈니스 에러: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("지역별 캠페인 검색 실패 - 시스템 에러: region={}, category={}, error={}",
                    searchDto.getRegion(), searchDto.getRegionCategory(), e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 키워드 + 필터로 캠페인 검색
     */
    public Page<CampaignResponseDTO> searchWithFilters(CampaignFilterDTO filter, Pageable pageable) {
        log.info("키워드+필터 캠페인 검색 시작: keyword={}, hasFilters={}", 
                filter.getKeyword(), filter.hasCheckboxFilters());

        try {
            // 필터 유효성 검증
            validateFilterSearch(filter);

            // SNS 플랫폼 데이터 추출 (null-safe) -> 0이여도 상관 없음.
            SocialPlatformDTO sns = filter.getSns();

            Page<Campaign> campaigns = campaignRepository.findByKeywordAndFilters(
                    trimKeyword(filter.getKeyword()),
                    filter.getRegion1(),
                    filter.getRegion2(),
                    filter.getProduct(),
                    filter.getReporter(),
                    sns != null ? sns.getYoutube() : 0,
                    sns != null ? sns.getShorts() : 0,
                    sns != null ? sns.getInsta() : 0,
                    sns != null ? sns.getReels() : 0,
                    sns != null ? sns.getBlog() : 0,
                    sns != null ? sns.getClip() : 0,
                    sns != null ? sns.getTiktok() : 0,
                    sns != null ? sns.getEtc() : 0,
                    getSearchStartDate(filter.getDeadlineStart()),  // null = 최소 날짜
                    getSearchEndDate(filter.getDeadlineEnd()),      // null = 최대 날짜
                    pageable);

            log.info("키워드+필터 캠페인 검색 완료: 총 {}건", campaigns.getTotalElements());
            return campaigns.map(CampaignResponseDTO::from);

        } catch (CampaignException e) {
            log.error("키워드+필터 검색 실패 - 비즈니스 에러: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("키워드+필터 검색 실패 - 시스템 에러: keyword={}, error={}",
                    filter.getKeyword(), e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 캠페인 상세 조회
     */
    public CampaignResponseDTO getCampaign(Long id) {
        log.info("캠페인 상세 조회 시작: id={}", id);
        
        try {
            // ID 유효성 검증
            validateCampaignId(id);
            
            // 캠페인 조회
            Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("캠페인을 찾을 수 없음: id={}", id);
                        return new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND);
                    });
            
            // 활성화 상태 체크
            if (!campaign.isActive()) {
                log.error("비활성화된 캠페인 접근 시도: id={}", id);
                throw new CampaignException(ErrorMessage.CAMPAIGN_INACTIVE);
            }
            
            log.info("캠페인 상세 조회 완료: id={}, title={}", id, campaign.getTitle());
            return CampaignResponseDTO.from(campaign);

        } catch (CampaignException e) {
            log.error("캠페인 상세 조회 실패 - 비즈니스 에러: id={}, error={}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("캠페인 상세 조회 실패 - 시스템 에러: id={}, error={}", id, e.getMessage(), e);
            throw new CampaignException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 지역 검색 유효성 검증
     */
    private void validateRegionSearch(RegionSearchDTO searchDto) {
        if (searchDto == null) {
            log.error("지역 검색 DTO가 null입니다");
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }

        if (!searchDto.isValid()) {
            log.error("지역 검색 조건이 유효하지 않음: region={}, category={}", 
                    searchDto.getRegion(), searchDto.getRegionCategory());
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }

    /**
     * 필터 검색 유효성 검증
     */
    private void validateFilterSearch(CampaignFilterDTO filter) {
        if (filter == null) {
            log.error("필터 DTO가 null입니다");
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }

        // 키워드 검증
        if (filter.hasKeywordFilter()) {
            String trimmedKeyword = trimKeyword(filter.getKeyword());
            if (trimmedKeyword.isEmpty()) {
                log.error("키워드가 공백입니다: keyword='{}'", filter.getKeyword());
                throw new CampaignException(ErrorMessage.CAMPAIGN_KEYWORD_EMPTY);
            }
            if (trimmedKeyword.length() > MAX_KEYWORD_LENGTH) {
                log.error("키워드가 너무 깁니다: length={}", trimmedKeyword.length());
                throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
            }
        }

        // 빈 필터 체크
        if (filter.isEmpty()) {
            log.error("모든 검색 조건이 비어있습니다");
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }

    /**
     * 캠페인 ID 유효성 검증
     */
    private void validateCampaignId(Long id) {
        if (id == null || id <= 0) {
            log.error("유효하지 않은 캠페인 ID: id={}", id);
            throw new CampaignException(ErrorMessage.INVALID_PARAMETER);
        }
    }

    /**
     * 지역-카테고리 조합 검증
     */
    private void validateRegionCategoryCombo(Region region, RegionCategory category) {
        // 현재는 모든 지역에서 모든 카테고리 사용 가능
        // 향후 특정 조합 제한이 필요하면 여기에 추가
        log.debug("지역-카테고리 조합 검증 완료: region={}, category={}", region, category);
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