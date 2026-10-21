package com.project.dtro.resource.controller;

import com.project.dtro.resource.dto.ResourceRequest;
import com.project.dtro.resource.dto.ResourceResponse;
import com.project.dtro.resource.dto.BulkResourcePublicRequest;
import com.project.dtro.resource.service.ResourceFileStorage;
import com.project.dtro.resource.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceFileStorage fileStorage;

    @GetMapping
    public ResponseEntity<Page<ResourceResponse>> getResourceList(
            @RequestParam(value = "publicYn", required = false) Boolean publicYn,
            @RequestParam(value = "publicYnOnly", required = false) Boolean publicYnOnly,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", defaultValue = "all") String searchType,
            Pageable pageable,
            Authentication authentication) {
        Boolean requestedPublicYn = publicYn != null ? publicYn : publicYnOnly;
        boolean canViewPrivate = canViewPrivate(authentication);
        Boolean targetPublicYn;
        if (canViewPrivate) {
            targetPublicYn = requestedPublicYn;
        } else {
            targetPublicYn = Boolean.TRUE;
        }
        return ResponseEntity.ok(resourceService.getResourceList(targetPublicYn, keyword, searchType, pageable));
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable Long resourceId, Authentication authentication) {
        return ResponseEntity.ok(resourceService.getResource(resourceId, canViewPrivate(authentication)));
    }

    @GetMapping("/{resourceId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long resourceId,
            @PathVariable Long attachmentId,
            Authentication authentication) {
        var attachment = resourceService.getAttachment(resourceId, attachmentId, canViewPrivate(authentication));
        try {
            var path = fileStorage.resolve(attachment.getFilePath());
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "첨부 파일을 찾을 수 없습니다.");
            }
            Resource file = new FileSystemResource(path);
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(attachment.getOriginalName(), StandardCharsets.UTF_8).build().toString())
                    .body(file);
        } catch (IOException error) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "첨부 파일을 찾을 수 없습니다.", error);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestPart("data") ResourceRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(request, files));
    }

    @PutMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.updateResource(resourceId, request));
    }

    @PutMapping(value = "/{resourceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
    public ResponseEntity<ResourceResponse> updateResourceWithAttachments(
            @PathVariable Long resourceId,
            @Valid @RequestPart("data") ResourceRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(resourceService.updateResource(resourceId, request, files));
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bulk-public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> updatePublicYn(@Valid @RequestBody BulkResourcePublicRequest request) {
        return ResponseEntity.ok(resourceService.updatePublicYn(request.getIds(), request.getPublicYn()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> deleteResources(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(resourceService.deleteResources(ids));
    }

    private boolean canViewPrivate(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TRAIN_MANAGER".equals(authority.getAuthority()));
    }
}
