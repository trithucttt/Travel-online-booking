package com.trithuc.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStoreService {
    void init();

    void saveImage(MultipartFile file, Long identifier, String type);

    List<String> getAllImageNames(String type);

    Resource loadImage(String filename, String type);

    void deleteOldImage(String type, Long identifier);
}
