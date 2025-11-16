package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.domain.CampaignStatusCase;
import com.example.cherrydan.campaign.domain.Bookmark;
import com.example.cherrydan.campaign.dto.*;
import com.example.cherrydan.common.response.PageListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import com.example.cherrydan.campaign.repository.CampaignRepository;
import com.example.cherrydan.campaign.repository.CampaignStatusRepository;
import com.example.cherrydan.campaign.repository.BookmarkRepository;
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
    private final BookmarkRepository bookmarkRepository;

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
    public CampaignStatusPopupByTypeResponseDTO getPopupStatusByBookmark(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        List<Bookmark> bookmarks = bookmarkRepository.findActiveBookmarksByUserForPopup(user.getId(), today);

        List<CampaignStatusPopupItemDTO> items = new ArrayList<>();
        
        for (Bookmark bookmark : bookmarks) {
            try {
                CampaignStatusPopupItemDTO dto = CampaignStatusPopupItemDTO.fromBookmark(bookmark);
                items.add(dto);
            } catch (BaseException e) {
                // 에러 발생 시 해당 항목은 건너뛰고 계속 진행
                continue;
            }
        }
        
        return CampaignStatusPopupByTypeResponseDTO.builder()
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
        CampaignStatusType statusType = statusCase.toStatusType();
        
        // 1단계: ID만 페이징해서 가져오기
        Page<Long> idPage = switch (statusCase) {
            case APPLIED_WAITING -> campaignStatusRepository.findIdsByUserAndAppliedWaiting(user, statusType, today, pageable);
            case APPLIED_COMPLETED -> campaignStatusRepository.findIdsByUserAndAppliedCompleted(user, statusType, today, pageable);
            case RESULT_SELECTED -> campaignStatusRepository.findIdsByUserAndResultSelected(user, statusType, today, pageable);
            case RESULT_NOT_SELECTED -> campaignStatusRepository.findIdsByUserAndResultNotSelected(user, statusType, today, pageable);
            case REVIEW_IN_PROGRESS -> campaignStatusRepository.findIdsByUserAndReviewInProgress(user, statusType, today, pageable);
            case REVIEW_COMPLETED -> campaignStatusRepository.findIdsByUserAndReviewCompleted(user, statusType, pageable);
        };
        
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
                .appliedWaiting(campaignStatusRepository.countByUserAndAppliedWaiting(user, CampaignStatusCase.APPLIED_WAITING.toStatusType(), today))
                .appliedCompleted(campaignStatusRepository.countByUserAndAppliedCompleted(user, CampaignStatusCase.APPLIED_COMPLETED.toStatusType(), today))
                .resultSelected(campaignStatusRepository.countByUserAndResultSelected(user, CampaignStatusCase.RESULT_SELECTED.toStatusType(), today))
                .resultNotSelected(campaignStatusRepository.countByUserAndResultNotSelected(user, CampaignStatusCase.RESULT_NOT_SELECTED.toStatusType(), today))
                .reviewInProgress(campaignStatusRepository.countByUserAndReviewInProgress(user, CampaignStatusCase.REVIEW_IN_PROGRESS.toStatusType(), today))
                .reviewCompleted(campaignStatusRepository.countByUserAndReviewCompleted(user, CampaignStatusCase.REVIEW_COMPLETED.toStatusType()))
                .build();
    }
}