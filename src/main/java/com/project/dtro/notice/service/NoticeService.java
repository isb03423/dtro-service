package com.project.dtro.notice.service;

import com.project.dtro.notice.dto.NoticeRequest;
import com.project.dtro.notice.dto.NoticeResponse;
import com.project.dtro.notice.domain.Notice; // 도메인이 아닌 entity 패키지의 Notice 사용
import com.project.dtro.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeResponse> getNoticeList(Boolean publicYn, String keyword, String searchType, Pageable pageable) {
        String normalizedSearchType = normalizeSearchType(searchType);
        Page<Notice> noticePage = noticeRepository.findByFilters(publicYn, keyword, normalizedSearchType, pageable);
        return noticePage.map(NoticeResponse::from);
    }

    private String normalizeSearchType(String searchType) {
        if ("title".equalsIgnoreCase(searchType)) return "title";
        if ("content".equalsIgnoreCase(searchType)) return "content";
        return "all";
    }

    public NoticeResponse getNoticeDetail(Long id, boolean canViewPrivate) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다. ID: " + id));
        if (!canViewPrivate && !Boolean.TRUE.equals(notice.getPublicYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 공지사항은 관리자만 확인할 수 있습니다.");
        }
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .importantYn(request.isImportantYn())
                .publicYn(request.isPublicYn())
                .publishStartAt(request.getPublishStartAt() != null ? request.getPublishStartAt() : LocalDateTime.now())
                .publishEndAt(request.getPublishEndAt())
                .build();
        Notice saved = noticeRepository.save(notice);
        return NoticeResponse.from(saved);
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다. ID: " + id));

        notice.update(
                request.getTitle(),
                request.getContent(),
                request.isImportantYn(),
                request.isPublicYn(),
                request.getPublishStartAt() != null ? request.getPublishStartAt() : notice.getPublishStartAt(),
                request.getPublishEndAt()
        );
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다. ID: " + id));
        noticeRepository.delete(notice);
    }

    @Transactional
    public int updatePublicYn(List<Long> ids, Boolean publicYn) {
        List<Notice> notices = noticeRepository.findAllById(ids);
        if (notices.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("선택한 공지사항 중 일부를 찾을 수 없습니다.");
        }
        notices.forEach(notice -> notice.updatePublicYn(publicYn));
        return notices.size();
    }

    @Transactional
    public int deleteNotices(List<Long> ids) {
        List<Notice> notices = noticeRepository.findAllById(ids);
        if (notices.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("선택한 공지사항 중 일부를 찾을 수 없습니다.");
        }
        noticeRepository.deleteAllInBatch(notices);
        return notices.size();
    }
}
