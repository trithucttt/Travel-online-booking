package com.trithuc.service.impl;

import com.trithuc.service.FileStoreService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStoreServiceImpl implements FileStoreService {

    private final Path rootLocation = Paths.get("uploads");

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initial storage");
        }
    }

    @Override
    public void saveImage(MultipartFile file, Long identifier, String type) {
        try {
            Path destinationDirectory = this.rootLocation.resolve(type);
            if (!Files.exists(destinationDirectory)) {
                Files.createDirectories(destinationDirectory);
            }
            Path destinationFile = destinationDirectory.resolve(identifier + '_' + file.getOriginalFilename());
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store File", e);
        }
    }

    @Override
    public List<String> getAllImageNames(String type) {
        try {
            Path destinationDirectory = this.rootLocation.resolve(type);
            if (Files.exists(destinationDirectory)) {
                return Files.walk(destinationDirectory, 1)
                        .filter(path -> !path.equals(destinationDirectory))
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
        return List.of(); // Trả về danh sách rỗng nếu không tìm thấy
    }

    @Override
    public Resource loadImage(String filename, String type) {
        try {
            Path file = rootLocation.resolve(type).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error " + e.getMessage());
        }
    }

    @Override
    public void deleteOldImage(String type, Long identifier) {

    }

}
