package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
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
    public CampaignStatusPopupResponseDTO getPopupStatusByUser(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        List<CampaignStatusPopupItemDTO> apply = new ArrayList<>();
        List<CampaignStatusPopupItemDTO> selected = new ArrayList<>();
        List<CampaignStatusPopupItemDTO> reviewing = new ArrayList<>();
        List<CampaignStatus> all = campaignStatusRepository.findByUserAndIsActiveTrue(user);
        for (CampaignStatus status : all) {
            try {
                CampaignStatusPopupItemDTO dto = CampaignStatusPopupItemDTO.fromEntity(status);
                switch (status.getStatus()) {
                    case APPLY: apply.add(dto); break;
                    case SELECTED: selected.add(dto); break;
                    case REVIEWING: reviewing.add(dto); break;
                    default: break;
                }
            } catch (BaseException e) {
                // 캠페인 정보가 없는 데이터는 무시
            }
        }
        LocalDate today = LocalDate.now();
        List<CampaignStatusPopupItemDTO> filteredApply = apply.stream()
            .filter(dto -> dto.getReviewerAnnouncementStatus() != null)
            .sorted(Comparator.comparing(CampaignStatusPopupItemDTO::getReviewerAnnouncementStatus))
            .toList();
        List<CampaignStatusPopupItemDTO> filteredSelected = selected.stream()
            .filter(dto -> dto.getReviewerAnnouncementStatus() != null)
            .sorted(Comparator.comparing(CampaignStatusPopupItemDTO::getReviewerAnnouncementStatus))
            .toList();
        List<CampaignStatusPopupItemDTO> filteredReviewing = reviewing.stream()
            .filter(dto -> dto.getReviewerAnnouncementStatus() != null)
            .sorted(Comparator.comparing(CampaignStatusPopupItemDTO::getReviewerAnnouncementStatus))
            .toList();
        return CampaignStatusPopupResponseDTO.builder()
                .applyTotal(filteredApply.size())
                .selectedTotal(filteredSelected.size())
                .reviewingTotal(filteredReviewing.size())
                .apply(filteredApply.stream().limit(4).toList())
                .selected(filteredSelected.stream().limit(4).toList())
                .reviewing(filteredReviewing.stream().limit(4).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageListResponseDTO<CampaignStatusResponseDTO> getStatusesByType(Long userId, CampaignStatusType statusType, Pageable pageable) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Page<CampaignStatus> page = campaignStatusRepository.findByUserAndStatusAndIsActiveTrue(user, statusType, pageable);
        List<CampaignStatusResponseDTO> content = page.getContent().stream()
                .map(CampaignStatusResponseDTO::fromEntity)
                .toList();
        return PageListResponseDTO.<CampaignStatusResponseDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStatusCountResponseDTO getStatusCounts(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        return CampaignStatusCountResponseDTO.builder()
                .apply(campaignStatusRepository.countByUserAndStatusAndIsActiveTrue(user, CampaignStatusType.APPLY))
                .selected(campaignStatusRepository.countByUserAndStatusAndIsActiveTrue(user, CampaignStatusType.SELECTED))
                .notSelected(campaignStatusRepository.countByUserAndStatusAndIsActiveTrue(user, CampaignStatusType.NOT_SELECTED))
                .reviewing(campaignStatusRepository.countByUserAndStatusAndIsActiveTrue(user, CampaignStatusType.REVIEWING))
                .ended(campaignStatusRepository.countByUserAndStatusAndIsActiveTrue(user, CampaignStatusType.ENDED))
                .build();
    }
}