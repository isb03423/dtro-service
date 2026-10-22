package com.project.dtro.resource.service;

import com.project.dtro.resource.domain.Resource;
import com.project.dtro.resource.domain.Attachment;
import com.project.dtro.resource.dto.ResourceRequest;
import com.project.dtro.resource.dto.ResourceResponse;
import com.project.dtro.resource.repository.ResourceRepository;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceFileStorage fileStorage;

    public Page<ResourceResponse> getResourceList(Boolean publicYn, String keyword, String searchType, Pageable pageable) {
        String normalizedSearchType = normalizeSearchType(searchType);
        Pageable sortedPageable = pageable.getSort().isSorted() ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "resourceId"));
        return resourceRepository.findByFilters(publicYn, keyword, normalizedSearchType, sortedPageable).map(ResourceResponse::from);
    }

    private String normalizeSearchType(String searchType) {
        if ("title".equalsIgnoreCase(searchType)) return "title";
        if ("content".equalsIgnoreCase(searchType)) return "content";
        return "all";
    }

    public ResourceResponse getResource(Long resourceId, boolean canViewPrivate) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자료입니다. ID: " + resourceId));
        if (!canViewPrivate && !Boolean.TRUE.equals(resource.getPublicYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 자료는 관리자만 확인할 수 있습니다.");
        }
        return ResourceResponse.from(resource, attachment -> fileStorage.size(attachment.getFilePath()));
    }

    public Attachment getAttachment(Long resourceId, Long attachmentId, boolean canViewPrivate) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자료입니다. ID: " + resourceId));
        if (!canViewPrivate && !Boolean.TRUE.equals(resource.getPublicYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 자료는 관리자만 확인할 수 있습니다.");
        }
        return resource.getAttachments().stream()
                .filter(attachment -> attachment.getAttachmentId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("첨부 파일을 찾을 수 없습니다."));
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request, List<MultipartFile> files) {
        Resource resource = Resource.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .publicYn(request.getPublicYn())
                .build();

        List<String> storedPaths = new ArrayList<>();
        try {
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;
                    String storedPath = fileStorage.store(file);
                    storedPaths.add(storedPath);
                    resource.addAttachment(Attachment.builder()
                            .originalName(file.getOriginalFilename() == null ? "첨부 파일" : file.getOriginalFilename())
                            .filePath(storedPath)
                            .build());
                }
            }
            return ResourceResponse.from(resourceRepository.saveAndFlush(resource),
                    attachment -> fileStorage.size(attachment.getFilePath()));
        } catch (IOException | RuntimeException error) {
            storedPaths.forEach(fileStorage::delete);
            throw error instanceof RuntimeException runtimeError
                    ? runtimeError
                    : new IllegalStateException("첨부 파일 저장에 실패했습니다.", error);
        }
    }

    @Transactional
    public ResourceResponse updateResource(Long resourceId, ResourceRequest request) {
        return updateResource(resourceId, request, null);
    }

    @Transactional
    public ResourceResponse updateResource(Long resourceId, ResourceRequest request, List<MultipartFile> files) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자료입니다. ID: " + resourceId));

        Set<Long> removalIds = new HashSet<>(request.getRemovedAttachmentIds() == null
                ? List.of() : request.getRemovedAttachmentIds());
        Set<Long> existingIds = resource.getAttachments().stream()
                .map(Attachment::getAttachmentId)
                .collect(Collectors.toSet());
        if (!existingIds.containsAll(removalIds)) {
            throw new IllegalArgumentException("삭제할 첨부 파일을 찾을 수 없습니다.");
        }

        resource.update(request.getTitle(), request.getContent(), request.getPublicYn());
        List<String> removedPaths = resource.getAttachments().stream()
                .filter(attachment -> removalIds.contains(attachment.getAttachmentId()))
                .map(Attachment::getFilePath)
                .toList();
        resource.getAttachments().removeIf(attachment -> removalIds.contains(attachment.getAttachmentId()));
        List<String> storedPaths = new ArrayList<>();
        try {
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;
                    String storedPath = fileStorage.store(file);
                    storedPaths.add(storedPath);
                    resource.addAttachment(Attachment.builder()
                            .originalName(file.getOriginalFilename() == null ? "첨부 파일" : file.getOriginalFilename())
                            .filePath(storedPath)
                            .build());
                }
            }
            ResourceResponse response = ResourceResponse.from(resourceRepository.saveAndFlush(resource),
                    attachment -> fileStorage.size(attachment.getFilePath()));
            if (!removedPaths.isEmpty()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        removedPaths.forEach(fileStorage::delete);
                    }
                });
            }
            return response;
        } catch (IOException | RuntimeException error) {
            storedPaths.forEach(fileStorage::delete);
            throw error instanceof RuntimeException runtimeError
                    ? runtimeError
                    : new IllegalStateException("첨부 파일 저장에 실패했습니다.", error);
        }
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자료입니다. ID: " + resourceId));
        resource.getAttachments().forEach(attachment -> fileStorage.delete(attachment.getFilePath()));
        resourceRepository.delete(resource);
    }

    @Transactional
    public int updatePublicYn(List<Long> ids, Boolean publicYn) {
        List<Resource> resources = resourceRepository.findAllById(ids);
        if (resources.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("선택한 자료 중 일부를 찾을 수 없습니다.");
        }
        resources.forEach(resource -> resource.updatePublicYn(publicYn));
        return resources.size();
    }

    @Transactional
    public int deleteResources(List<Long> ids) {
        List<Resource> resources = resourceRepository.findAllById(ids);
        if (resources.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("선택한 자료 중 일부를 찾을 수 없습니다.");
        }
        resources.forEach(resource -> resource.getAttachments()
                .forEach(attachment -> fileStorage.delete(attachment.getFilePath())));
        resourceRepository.deleteAll(resources);
        return resources.size();
    }
}
