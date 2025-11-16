package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.domain.CampaignStatusCase;
import com.example.cherrydan.campaign.dto.*;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.campaign.domain.CampaignPlatformType;
import com.example.cherrydan.campaign.domain.SnsPlatformType;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import com.example.cherrydan.common.exception.BaseException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.common.exception.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class CampaignStatusServiceImpl implements CampaignStatusService {
    private final CampaignStatusRepository campaignStatusRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CampaignStatusResponseDTO createOrRecoverStatus(CampaignStatusRequestDTO requestDTO, Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(requestDTO.getCampaignId())
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));

        Optional<CampaignStatus> optional = campaignStatusRepository.findByUserAndCampaign(user, campaign);

        CampaignStatus status;
        if (optional.isPresent()) {
            status = optional.get();
            status.setIsActive(true);
            status.setStatus(requestDTO.getStatus());
        } else {
            status = CampaignStatus.builder()
                    .user(user)
                    .campaign(campaign)
                    .status(requestDTO.getStatus())
                    .isActive(true)
                    .build();
        }
        CampaignStatus saved = campaignStatusRepository.save(status);
        return CampaignStatusResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<CampaignStatusResponseDTO> updateStatusBatch(CampaignStatusBatchRequestDTO requestDTO, Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        if (requestDTO.getStatus() != null) {
            if (requestDTO.getIsActive() != null) {
                campaignStatusRepository.updateStatusAndActiveBatch(user, requestDTO.getCampaignIds(), 
                                                                   requestDTO.getIsActive(), requestDTO.getStatus());
            } else {
                campaignStatusRepository.updateStatusOnlyBatch(user, requestDTO.getCampaignIds(), 
                                                              requestDTO.getStatus());
            }
        }
        
        List<CampaignStatus> updatedStatuses = campaignStatusRepository.findByUserAndCampaignIds(user, requestDTO.getCampaignIds());
        return updatedStatuses.stream()
                .map(CampaignStatusResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStatusBatch(CampaignStatusDeleteRequestDTO requestDTO, Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        campaignStatusRepository.deleteByUserAndCampaignIds(user, requestDTO.getCampaignIds());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStatusPopupByTypeResponseDTO getPopupStatusByType(Long userId, CampaignStatusType statusType) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        List<CampaignStatus> statuses = campaignStatusRepository.findTop4ByUserAndStatusAndExpired(user, statusType, today);

        List<CampaignStatusPopupItemDTO> items = new ArrayList<>();
        
        for (CampaignStatus status : statuses) {
            try {
                CampaignStatusPopupItemDTO dto = CampaignStatusPopupItemDTO.fromEntity(status);
                items.add(dto);
            } catch (BaseException e) {
                continue;
            }
        }
        
        return CampaignStatusPopupByTypeResponseDTO.builder()
                .statusType(statusType)
                .items(items)
                .totalCount(items.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageListResponseDTO<CampaignStatusResponseDTO> getStatusesByCase(Long userId, CampaignStatusCase statusCase, Pageable pageable) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        Page<Long> idPage;
        
        // 1단계: ID만 페이징해서 가져오기
        switch (statusCase) {
            case APPLIED_WAITING:
                idPage = campaignStatusRepository.findIdsByUserAndAppliedWaiting(user, CampaignStatusType.APPLY, today, pageable);
                break;
            case APPLIED_COMPLETED:
                idPage = campaignStatusRepository.findIdsByUserAndAppliedCompleted(user, CampaignStatusType.APPLY, today, pageable);
                break;
            case RESULT_SELECTED:
                idPage = campaignStatusRepository.findIdsByUserAndResultSelected(user, CampaignStatusType.SELECTED, today, pageable);
                break;
            case RESULT_NOT_SELECTED:
                idPage = campaignStatusRepository.findIdsByUserAndResultNotSelected(user, CampaignStatusType.NOT_SELECTED, today, pageable);
                break;
            case REVIEW_IN_PROGRESS:
                idPage = campaignStatusRepository.findIdsByUserAndReviewInProgress(user, CampaignStatusType.REVIEWING, today, pageable);
                break;
            case REVIEW_COMPLETED:
                idPage = campaignStatusRepository.findIdsByUserAndReviewCompleted(user, CampaignStatusType.ENDED, pageable);
                break;
            default:
                throw new BaseException(ErrorMessage.RESOURCE_NOT_FOUND);
        }
        
        // ID가 없으면 빈 리스트 반환
        if (idPage.getContent().isEmpty()) {
            return PageListResponseDTO.<CampaignStatusResponseDTO>builder()
                    .content(List.of())
                    .page(idPage.getNumber())
                    .size(idPage.getSize())
                    .totalElements(idPage.getTotalElements())
                    .totalPages(idPage.getTotalPages())
                    .hasNext(idPage.hasNext())
                    .hasPrevious(idPage.hasPrevious())
                    .build();
        }
        
        // 2단계: ID 리스트로 JOIN FETCH하여 실제 데이터 가져오기 (N+1 문제 방지)
        List<CampaignStatus> statuses = campaignStatusRepository.findByIdsWithFetch(idPage.getContent());
        
        // ID 순서를 유지하기 위해 Map으로 변환 후 정렬
        Map<Long, CampaignStatus> statusMap = statuses.stream()
                .collect(Collectors.toMap(CampaignStatus::getId, status -> status));
        
        // ID 순서대로 정렬
        List<CampaignStatus> orderedStatuses = idPage.getContent().stream()
                .map(statusMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<CampaignStatusResponseDTO> content = orderedStatuses.stream()
                .map(CampaignStatusResponseDTO::fromEntity)
                .toList();
        
        return PageListResponseDTO.<CampaignStatusResponseDTO>builder()
                .content(content)
                .page(idPage.getNumber())
                .size(idPage.getSize())
                .totalElements(idPage.getTotalElements())
                .totalPages(idPage.getTotalPages())
                .hasNext(idPage.hasNext())
                .hasPrevious(idPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStatusCountResponseDTO getStatusCounts(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        return CampaignStatusCountResponseDTO.builder()
                .appliedWaiting(campaignStatusRepository.countByUserAndAppliedWaiting(user, CampaignStatusType.APPLY, today))
                .appliedCompleted(campaignStatusRepository.countByUserAndAppliedCompleted(user, CampaignStatusType.APPLY, today))
                .resultSelected(campaignStatusRepository.countByUserAndResultSelected(user, CampaignStatusType.SELECTED, today))
                .resultNotSelected(campaignStatusRepository.countByUserAndResultNotSelected(user, CampaignStatusType.NOT_SELECTED, today))
                .reviewInProgress(campaignStatusRepository.countByUserAndReviewInProgress(user, CampaignStatusType.REVIEWING, today))
                .reviewCompleted(campaignStatusRepository.countByUserAndReviewCompleted(user, CampaignStatusType.ENDED))
                .build();
    }
}