package com.example.cherrydan.inquiry.service;

import com.example.cherrydan.common.exception.BaseException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.InquiryException;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.inquiry.domain.Inquiry;
import com.example.cherrydan.inquiry.domain.InquiryCategory;
import com.example.cherrydan.inquiry.domain.InquiryStatus;
import com.example.cherrydan.inquiry.dto.InquiryRequestDTO;
import com.example.cherrydan.inquiry.dto.InquiryResponseDTO;
import com.example.cherrydan.inquiry.repository.InquiryRepository;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 문의 목록 조회
     */
    public Page<InquiryResponseDTO> getUserInquiries(Long userId, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        log.info("사용자 문의 목록 조회 완료: userId={}, totalElements={}",
                userId, inquiries.getTotalElements());

        return inquiries.map(InquiryResponseDTO::from);
    }

    /**
     * 상태별 사용자 문의 목록 조회
     */
    public Page<InquiryResponseDTO> getUserInquiriesByStatus(Long userId, InquiryStatus status, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);

        log.info("상태별 사용자 문의 목록 조회 완료: userId={}, status={}, totalElements={}",
                userId, status.getDescription(), inquiries.getTotalElements());

        return inquiries.map(InquiryResponseDTO::from);
    }

    /**
     * 1:1 문의 등록
     */
    @Transactional
    public InquiryResponseDTO createInquiry(Long userId, InquiryRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        // 카테고리 파싱
        InquiryCategory category = InquiryCategory.fromString(requestDTO.getCategory());

        try {
            Inquiry inquiry = Inquiry.builder()
                    .user(user)
                    .category(category)
                    .title(requestDTO.getTitle())
                    .content(requestDTO.getContent())
                    .build();

            Inquiry savedInquiry = inquiryRepository.save(inquiry);

            log.info("문의 등록 완료: inquiryId={}, userId={}, category={}",
                    savedInquiry.getId(), userId, category.getDescription());

            return InquiryResponseDTO.from(savedInquiry);
        } catch (Exception e) {
            log.error("문의 등록 실패: userId={}, error={}", userId, e.getMessage());
            throw new InquiryException(ErrorMessage.INQUIRY_CREATE_FAILED);
        }
    }

    public InquiryResponseDTO getInquiryDetail(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(ErrorMessage.INQUIRY_NOT_FOUND));

        // 본인의 문의인지 확인
        if (!inquiry.getUser().getId().equals(userId)) {
            throw new InquiryException(ErrorMessage.INQUIRY_ACCESS_DENIED);
        }

        return InquiryResponseDTO.from(inquiry);
    }

    @Transactional
    public InquiryResponseDTO replyToInquiry(Long inquiryId, String adminReply, Long adminId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(ErrorMessage.INQUIRY_NOT_FOUND));

        try {
            inquiry.reply(adminReply, adminId);
            Inquiry savedInquiry = inquiryRepository.save(inquiry);

            log.info("관리자 답변 등록 완료: inquiryId={}, adminId={}", inquiryId, adminId);

            return InquiryResponseDTO.from(savedInquiry);
        } catch (Exception e) {
            log.error("관리자 답변 등록 실패: inquiryId={}, error={}", inquiryId, e.getMessage());
            throw new InquiryException(ErrorMessage.INQUIRY_REPLY_FAILED);
        }
    }
}
