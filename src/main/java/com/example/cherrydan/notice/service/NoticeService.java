package com.example.cherrydan.notice.service;

import com.example.cherrydan.common.exception.BaseException;
import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.NoticeException;
import com.example.cherrydan.notice.domain.Notice;
import com.example.cherrydan.notice.domain.NoticeCategory;
import com.example.cherrydan.notice.dto.NoticeResponseDTO;
import com.example.cherrydan.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 활성화된 공지사항 목록 조회 (고정글 우선, 발행일 순)
     */
    public Page<NoticeResponseDTO> getActiveNotices(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findActiveNoticesOrderByPinnedAndPublishedAt(pageable);

        log.info("공지사항 목록 조회 완료: totalElements={}", notices.getTotalElements());

        return notices.map(NoticeResponseDTO::from);
    }

    /**
     * 카테고리별 공지사항 목록 조회
     */
    public Page<NoticeResponseDTO> getActiveNoticesByCategory(NoticeCategory category, Pageable pageable) {
        Page<Notice> notices = noticeRepository.findActiveNoticesByCategoryOrderByPinnedAndPublishedAt(category, pageable);

        log.info("카테고리별 공지사항 조회 완료: category={}, totalElements={}",
                category.getDescription(), notices.getTotalElements());

        return notices.map(NoticeResponseDTO::from);
    }

    /**
     * 공지사항 상세 조회 (조회수 증가)
     */
    @Transactional
    public NoticeResponseDTO getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorMessage.NOTICE_NOT_FOUND));

        if (!notice.getIsActive()) {
            throw new NoticeException(ErrorMessage.NOTICE_INACTIVE);
        }

        // 조회수 증가
        noticeRepository.incrementViewCount(id);
        log.info("공지사항 상세 조회 완료: id={}, title={}, viewCount={}",
                id, notice.getTitle(), notice.getViewCount());

        return NoticeResponseDTO.from(notice);
    }

    /**
     * 공감 버튼 클릭 시 공감 지수 증가
     */
    @Transactional
    public NoticeResponseDTO incrementEmpathyCount(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorMessage.NOTICE_NOT_FOUND));

        if (!notice.getIsActive()) {
            throw new NoticeException(ErrorMessage.NOTICE_INACTIVE);
        }

        // 공감 지수 증가
        noticeRepository.incrementEmpathyCount(id);
        log.info("공지사항 공감 지수 증가 완료: id={}, title={}, empathyCount={}",
                id, notice.getTitle(), notice.getEmpathyCount());

        return NoticeResponseDTO.from(notice);
    }

    /**
     * 고정된 공지사항 목록 조회
     */
    public List<NoticeResponseDTO> getPinnedNotices() {
        List<Notice> pinnedNotices = noticeRepository.findActivePinnedNotices();

        log.info("고정 공지사항 조회 완료: count={}", pinnedNotices.size());

        return pinnedNotices.stream()
                .map(NoticeResponseDTO::from)
                .collect(Collectors.toList());
    }
}
