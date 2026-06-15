package com.parcinformatique.app.service;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeEquipmentImage(UUID equipmentId, MultipartFile file);

    String storeEquipmentDocument(UUID equipmentId, MultipartFile file);
}
