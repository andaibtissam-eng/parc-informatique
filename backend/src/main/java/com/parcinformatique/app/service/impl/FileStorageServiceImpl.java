package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of("application/pdf");

    private final Path basePath;

    public FileStorageServiceImpl(@Value("${app.upload.base-path:uploads}") String uploadBasePath) {
        this.basePath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
    }

    @Override
    public String storeEquipmentImage(UUID equipmentId, MultipartFile file) {
        validateFile(file, IMAGE_CONTENT_TYPES, "image");
        return store(equipmentId, "images", file, "image");
    }

    @Override
    public String storeEquipmentDocument(UUID equipmentId, MultipartFile file) {
        validateFile(file, DOCUMENT_CONTENT_TYPES, "document PDF");
        return store(equipmentId, "documents", file, "document");
    }

    private String store(UUID equipmentId, String folder, MultipartFile file, String prefix) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            Path targetDirectory = basePath.resolve("equipments").resolve(equipmentId.toString()).resolve(folder);
            Files.createDirectories(targetDirectory);
            String filename = prefix + "-" + System.currentTimeMillis() + extension;
            Path targetFile = targetDirectory.resolve(filename).normalize();
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/equipments/" + equipmentId + "/" + folder + "/" + filename;
        } catch (IOException ex) {
            throw new BusinessException("Impossible d'enregistrer le fichier");
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Aucun fichier " + label + " fourni");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new BusinessException("Type de fichier non autorise pour " + label);
        }
    }

    private String getExtension(String filename) {
        String clean = StringUtils.hasText(filename) ? filename : "";
        int index = clean.lastIndexOf('.');
        return index >= 0 ? clean.substring(index) : "";
    }
}
