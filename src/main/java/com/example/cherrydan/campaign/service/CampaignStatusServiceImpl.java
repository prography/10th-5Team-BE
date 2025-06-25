package com.example.cherrydan.campaign.service;

import com.example.cherrydan.campaign.domain.Campaign;
import com.example.cherrydan.campaign.domain.CampaignStatus;
import com.example.cherrydan.campaign.domain.CampaignStatusType;
import com.example.cherrydan.campaign.dto.BookmarkResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusRequestDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusListResponseDTO;
import com.example.cherrydan.campaign.dto.CampaignStatusPopupResponseDTO;
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
    public CampaignStatusResponseDTO createOrRecoverStatus(CampaignStatusRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
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
        return toDTO(saved);
    }

    @Override
    @Transactional
    public CampaignStatusResponseDTO updateStatus(CampaignStatusRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(requestDTO.getCampaignId())
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        CampaignStatus status = campaignStatusRepository.findByUserAndCampaign(user, campaign)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        if (requestDTO.getIsActive() != null) {
            status.setIsActive(requestDTO.getIsActive());
        }
        if (requestDTO.getStatus() != null) {
            status.setStatus(requestDTO.getStatus());
        }
        CampaignStatus saved = campaignStatusRepository.save(status);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteStatus(Long campaignId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BaseException(ErrorMessage.RESOURCE_NOT_FOUND));
        Optional<CampaignStatus> status = campaignStatusRepository.findByUserAndCampaign(user, campaign);
        if (status.isEmpty()) {
            throw new BaseException(ErrorMessage.RESOURCE_NOT_FOUND);
        }
        campaignStatusRepository.delete(status.get());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStatusListResponseDTO getStatusListWithCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        List<CampaignStatusResponseDTO> apply = new ArrayList<>();
        List<CampaignStatusResponseDTO> selected = new ArrayList<>();
        List<CampaignStatusResponseDTO> registered = new ArrayList<>();
        List<CampaignStatusResponseDTO> ended = new ArrayList<>();
        List<CampaignStatus> all = campaignStatusRepository.findByUserAndIsActiveTrue(user);
        for (CampaignStatus status : all) {
            switch (status.getStatus()) {
                case APPLY:
                    apply.add(toDTO(status));
                    break;
                case SELECTED:
                    selected.add(toDTO(status));
                    break;
                case REGISTERED:
                    registered.add(toDTO(status));
                    break;
                case ENDED:
                    ended.add(toDTO(status));
                    break;
                default:
                    break;
            }
        }
        // 1. 발표일 기준 정렬
        apply.sort(Comparator.comparing(
            CampaignStatusResponseDTO::getReviewerAnnouncement,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
        // 2. 콘텐츠 제출 종료일 기준 정렬
        selected.sort(Comparator.comparing(
            CampaignStatusResponseDTO::getContentSubmissionEnd,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
        // 3. 콘텐츠 제출 종료일 기준 정렬
        registered.sort(Comparator.comparing(
            CampaignStatusResponseDTO::getContentSubmissionEnd,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
        // 4. 결과 발표일 기준 정렬
        ended.sort(Comparator.comparing(
            CampaignStatusResponseDTO::getResultAnnouncement,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
        Map<String, Long> count = new HashMap<>();
        count.put("apply", (long) apply.size());
        count.put("selected", (long) selected.size());
        count.put("registered", (long) registered.size());
        count.put("ended", (long) ended.size());
        return CampaignStatusListResponseDTO.builder()
                .apply(apply)
                .selected(selected)
                .registered(registered)
                .ended(ended)
                .count(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStatusPopupResponseDTO getPopupStatusByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        List<CampaignStatusResponseDTO> apply = new ArrayList<>();
        List<CampaignStatusResponseDTO> selected = new ArrayList<>();
        List<CampaignStatusResponseDTO> registered = new ArrayList<>();
        List<CampaignStatus> all = campaignStatusRepository.findByUserAndIsActiveTrue(user);
        for (CampaignStatus status : all) {
            try {
                CampaignStatusResponseDTO dto = toDTO(status);
                switch (status.getStatus()) {
                    case APPLY: apply.add(dto); break;
                    case SELECTED: selected.add(dto); break;
                    case REGISTERED: registered.add(dto); break;
                    default: break;
                }
            } catch (BaseException e) {
                // 캠페인 정보가 없는 데이터는 무시
            }
        }
        LocalDate today = LocalDate.now();
        // 신청: reviewerAnnouncement 마감일이 지난 것만 오래된 순
        List<CampaignStatusResponseDTO> filteredApply = apply.stream()
            .filter(dto -> dto.getReviewerAnnouncement() != null && dto.getReviewerAnnouncement().isBefore(today))
            .sorted(Comparator.comparing(CampaignStatusResponseDTO::getReviewerAnnouncement))
            .toList();
        // 선정: contentSubmissionEnd 마감일이 지난 것만 오래된 순
        List<CampaignStatusResponseDTO> filteredSelected = selected.stream()
            .filter(dto -> dto.getContentSubmissionEnd() != null && dto.getContentSubmissionEnd().isBefore(today))
            .sorted(Comparator.comparing(CampaignStatusResponseDTO::getContentSubmissionEnd))
            .toList();
        // 등록: contentSubmissionEnd 마감일이 지난 것만 오래된 순
        List<CampaignStatusResponseDTO> filteredRegistered = registered.stream()
            .filter(dto -> dto.getContentSubmissionEnd() != null && dto.getContentSubmissionEnd().isBefore(today))
            .sorted(Comparator.comparing(CampaignStatusResponseDTO::getContentSubmissionEnd))
            .toList();
        return CampaignStatusPopupResponseDTO.builder()
                .applyTotal(filteredApply.size())
                .selectedTotal(filteredSelected.size())
                .registeredTotal(filteredRegistered.size())
                .apply(filteredApply.stream().limit(4).toList())
                .selected(filteredSelected.stream().limit(4).toList())
                .registered(filteredRegistered.stream().limit(4).toList())
                .build();
    }

    private CampaignStatusResponseDTO toDTO(CampaignStatus status) {
        String reviewerAnnouncementStatus = null;
        switch (status.getStatus()) {
            case APPLY:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getReviewerAnnouncement(), "apply");
                break;
            case SELECTED:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "selected");
                break;
            case REGISTERED:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getContentSubmissionEnd(), "registered");
                break;
            case ENDED:
                reviewerAnnouncementStatus = CampaignStatusResponseDTO.getStatusMessage(status.getCampaign().getResultAnnouncement(), "ended");
                break;
            default:
                break;
        }
        return CampaignStatusResponseDTO.builder()
                .id(status.getId())
                .campaignId(status.getCampaign().getId())
                .userId(status.getUser().getId())
                .statusLabel(status.getStatus().getLabel())
                .isActive(status.getIsActive())
                .title(status.getCampaign().getTitle())
                .detailUrl(status.getCampaign().getDetailUrl())
                .imageUrl(status.getCampaign().getImageUrl())
                .reviewerAnnouncement(status.getCampaign().getReviewerAnnouncement())
                .reviewerAnnouncementStatus(reviewerAnnouncementStatus)
                .applicantCount(status.getCampaign().getApplicantCount())
                .recruitCount(status.getCampaign().getRecruitCount())
                .snsPlatforms(com.example.cherrydan.campaign.dto.BookmarkResponseDTO.getPlatforms(status.getCampaign()))
                .campaignPlatform(com.example.cherrydan.campaign.dto.BookmarkResponseDTO.getCampaignPlatformLabel(status.getCampaign().getSourceSite()))
                .benefit(status.getCampaign().getBenefit())
                .contentSubmissionEnd(status.getCampaign().getContentSubmissionEnd())
                .resultAnnouncement(status.getCampaign().getResultAnnouncement())
                .build();
    }
}