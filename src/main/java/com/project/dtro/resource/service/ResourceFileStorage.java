package com.project.dtro.resource.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class ResourceFileStorage {

    private final Path root;

    public ResourceFileStorage(@Value("${file.upload-dir:./uploads}") String uploadDirectory) {
        this.root = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        Files.createDirectories(root);
        Path target = root.resolve(UUID.randomUUID().toString()).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("파일 경로가 올바르지 않습니다.");
        }
        file.transferTo(target);
        return target.toString();
    }

    public Path resolve(String storedPath) throws IOException {
        Path target = Path.of(storedPath).toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            throw new IOException("파일 경로가 올바르지 않습니다.");
        }
        return target;
    }

    public Long size(String storedPath) {
        try {
            return Files.size(resolve(storedPath));
        } catch (IOException error) {
            return null;
        }
    }

    public void delete(String storedPath) {
        try {
            Files.deleteIfExists(resolve(storedPath));
        } catch (IOException ignored) {
            // 데이터 삭제 작업을 파일 시스템의 일시 오류가 막지 않게 한다.
        }
    }
}
