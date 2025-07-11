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
        Page<Notice> notices = noticeRepository.findActiveNoticesOrderByHotAndPublishedAt(pageable);

        log.info("공지사항 목록 조회 완료: totalElements={}", notices.getTotalElements());

        return notices.map(NoticeResponseDTO::from);
    }

    /**
     * 카테고리별 공지사항 목록 조회
     */
    public Page<NoticeResponseDTO> getActiveNoticesByCategory(NoticeCategory category, Pageable pageable) {
        Page<Notice> notices = noticeRepository.findActiveNoticesByCategoryOrderByHotAndPublishedAt(category, pageable);

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
     * 공감 버튼 클릭 시 공감 지수 증가/감소
     */
    @Transactional
    public NoticeResponseDTO toggleEmpathy(Long id, boolean isEmpathy) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorMessage.NOTICE_NOT_FOUND));

        if (!notice.getIsActive()) {
            throw new NoticeException(ErrorMessage.NOTICE_INACTIVE);
        }

        if (isEmpathy) {
            noticeRepository.incrementEmpathyCount(id);
            log.info("공지사항 공감 지수 증가 완료: id={}, title={}, empathyCount={}",
                    id, notice.getTitle(), notice.getEmpathyCount() + 1);
        } else {
            noticeRepository.decrementEmpathyCount(id);
            log.info("공지사항 공감 지수 감소 완료: id={}, title={}, empathyCount={}",
                    id, notice.getTitle(), notice.getEmpathyCount() - 1);
        }

        // 업데이트된 공지사항 조회
        Notice updatedNotice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(ErrorMessage.NOTICE_NOT_FOUND));

        return NoticeResponseDTO.from(updatedNotice);
    }

    /**
     * 핫한 공지사항 목록 조회
     */
    public List<NoticeResponseDTO> getHotNotices() {
        List<Notice> hotNotices = noticeRepository.findActiveHotNotices();

        log.info("핫 공지사항 조회 완료: count={}", hotNotices.size());

        return hotNotices.stream()
                .map(NoticeResponseDTO::from)
                .collect(Collectors.toList());
    }
}
