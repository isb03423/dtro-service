package com.project.dtro.notice.controller;

import com.project.dtro.notice.dto.NoticeRequest;
import com.project.dtro.notice.dto.NoticeResponse;
import com.project.dtro.notice.dto.BulkNoticePublicRequest;
import com.project.dtro.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<Page<NoticeResponse>> getNoticeList(
            @RequestParam(required = false) Boolean publicYn,
            @RequestParam(required = false) Boolean publicYnOnly,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String searchType,
            Pageable pageable,
            Authentication authentication) {

        Boolean requestedPublicYn = (publicYn != null) ? publicYn : publicYnOnly;
        boolean canViewPrivate = canViewPrivate(authentication);
        // 비관리자는 공개 공지만 목록에서 조회할 수 있다.
        Boolean targetPublicYn;
        if (canViewPrivate) {
            targetPublicYn = requestedPublicYn;
        } else {
            targetPublicYn = Boolean.TRUE;
        }

        Page<NoticeResponse> notices = noticeService.getNoticeList(targetPublicYn, keyword, searchType, pageable);
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeDetail(@PathVariable Long id, Authentication authentication) {
        NoticeResponse notice = noticeService.getNoticeDetail(id, canViewPrivate(authentication));
        return ResponseEntity.ok(notice);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
    public ResponseEntity<NoticeResponse> createNotice(@RequestBody NoticeRequest request) {
        NoticeResponse created = noticeService.createNotice(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
    public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long id, @RequestBody NoticeRequest request) {
        NoticeResponse updated = noticeService.updateNotice(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bulk-public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> updatePublicYn(@Valid @RequestBody BulkNoticePublicRequest request) {
        return ResponseEntity.ok(noticeService.updatePublicYn(request.getIds(), request.getPublicYn()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> deleteNotices(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(noticeService.deleteNotices(ids));
    }

    private boolean canViewPrivate(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TRAIN_MANAGER".equals(authority.getAuthority()));
    }
}
