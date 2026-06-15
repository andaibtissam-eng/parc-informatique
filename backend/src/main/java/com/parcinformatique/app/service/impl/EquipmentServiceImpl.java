package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.equipment.EquipmentDetailDto;
import com.parcinformatique.app.dto.equipment.EquipmentSummaryDto;
import com.parcinformatique.app.dto.equipment.EquipmentUpsertRequest;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Category;
import com.parcinformatique.app.entity.Department;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.entity.Location;
import com.parcinformatique.app.entity.Supplier;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.EquipmentStatus;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.CategoryRepository;
import com.parcinformatique.app.repository.DepartmentRepository;
import com.parcinformatique.app.repository.EquipmentRepository;
import com.parcinformatique.app.repository.LocationRepository;
import com.parcinformatique.app.repository.SupplierRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.EquipmentService;
import com.parcinformatique.app.service.FileStorageService;
import com.parcinformatique.app.utils.CodeGeneratorUtil;
import com.parcinformatique.app.utils.QrCodeUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AuditService auditService;
    private final DomainMapper domainMapper;
    private final FileStorageService fileStorageService;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final QrCodeUtil qrCodeUtil;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EquipmentSummaryDto> listEquipments(String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Equipment> result = (search == null || search.isBlank())
            ? equipmentRepository.findAll(pageRequest)
            : equipmentRepository.findByNameContainingIgnoreCaseOrInventoryCodeContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                search, search, search, search, pageRequest);
        return new PaginatedResponse<>(
            result.getContent().stream().map(domainMapper::toEquipmentSummary).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDetailDto getEquipment(UUID equipmentId) {
        Equipment equipment = getEquipmentEntity(equipmentId);
        String qrCodeImage = equipment.getQrCode() != null ? qrCodeUtil.generateBase64Png(equipment.getQrCode()) : null;
        return domainMapper.toEquipmentDetail(equipment, qrCodeImage);
    }

    @Override
    @Transactional
    public EquipmentDetailDto createEquipment(EquipmentUpsertRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = new Equipment();
        equipment.setInventoryCode(codeGeneratorUtil.inventoryCode());
        applyEquipmentData(equipment, request);
        if (equipment.getQrCode() == null) {
            equipment.setQrCode("asset:" + equipment.getInventoryCode());
        }
        Equipment saved = equipmentRepository.save(equipment);
        writeActivity(actor, "create", "Equipment", saved.getId().toString(), "Ajout de l'equipement " + saved.getInventoryCode(), "bi bi-laptop", "success");
        auditService.log(actor, "CREATE_EQUIPMENT", "Equipment", saved.getId().toString(), "Equipement cree: " + saved.getInventoryCode(), ipAddress, userAgent);
        return getEquipment(saved.getId());
    }

    @Override
    @Transactional
    public EquipmentDetailDto updateEquipment(UUID equipmentId, EquipmentUpsertRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = getEquipmentEntity(equipmentId);
        validateStatusConsistency(equipment, request.status());
        applyEquipmentData(equipment, request);
        Equipment saved = equipmentRepository.save(equipment);
        writeActivity(actor, "update", "Equipment", saved.getId().toString(), "Mise a jour de l'equipement " + saved.getInventoryCode(), "bi bi-pencil-square", "info");
        auditService.log(actor, "UPDATE_EQUIPMENT", "Equipment", saved.getId().toString(), "Equipement mis a jour: " + saved.getInventoryCode(), ipAddress, userAgent);
        return getEquipment(saved.getId());
    }

    @Override
    @Transactional
    public EquipmentDetailDto uploadEquipmentImage(UUID equipmentId, org.springframework.web.multipart.MultipartFile file, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = getEquipmentEntity(equipmentId);
        equipment.setImageUrl(fileStorageService.storeEquipmentImage(equipmentId, file));
        Equipment saved = equipmentRepository.save(equipment);
        auditService.log(actor, "UPLOAD_EQUIPMENT_IMAGE", "Equipment", saved.getId().toString(), "Photo televersee pour " + saved.getInventoryCode(), ipAddress, userAgent);
        return getEquipment(saved.getId());
    }

    @Override
    @Transactional
    public EquipmentDetailDto uploadEquipmentDocument(UUID equipmentId, org.springframework.web.multipart.MultipartFile file, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = getEquipmentEntity(equipmentId);
        equipment.setDocumentUrl(fileStorageService.storeEquipmentDocument(equipmentId, file));
        Equipment saved = equipmentRepository.save(equipment);
        auditService.log(actor, "UPLOAD_EQUIPMENT_DOCUMENT", "Equipment", saved.getId().toString(), "Document televerse pour " + saved.getInventoryCode(), ipAddress, userAgent);
        return getEquipment(saved.getId());
    }

    private void applyEquipmentData(Equipment equipment, EquipmentUpsertRequest request) {
        validateStatusConsistency(equipment, request.status());
        equipment.setName(request.name());
        equipment.setBrand(request.brand());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber());
        equipment.setAssetTag(request.assetTag());
        equipment.setImageUrl(request.imageUrl());
        equipment.setDocumentUrl(request.documentUrl());
        equipment.setOperatingSystem(request.operatingSystem());
        equipment.setMemoryGb(request.memoryGb());
        equipment.setStorageGb(request.storageGb());
        equipment.setProcessor(request.processor());
        equipment.setPurchaseDate(request.purchaseDate());
        equipment.setWarrantyEndDate(request.warrantyEndDate());
        equipment.setAcquisitionCost(request.acquisitionCost());
        equipment.setStatus(request.status() != null ? request.status() : EquipmentStatus.AVAILABLE);
        equipment.setNotes(request.notes());
        equipment.setCategory(resolveCategory(request.categoryId()));
        equipment.setSupplier(resolveSupplier(request.supplierId()));
        equipment.setLocation(resolveLocation(request.locationId()));
        equipment.setDepartment(resolveDepartment(request.departmentId()));
        if (equipment.getQrCode() == null || equipment.getQrCode().isBlank()) {
            equipment.setQrCode("asset:" + (equipment.getInventoryCode() != null ? equipment.getInventoryCode() : codeGeneratorUtil.reference("QR")));
        }
    }

    private void validateStatusConsistency(Equipment equipment, EquipmentStatus targetStatus) {
        if (targetStatus == null) {
            return;
        }
        if (equipment.getActiveAssignment() != null && targetStatus == EquipmentStatus.AVAILABLE) {
            throw new BusinessException("Un equipement avec une affectation active ne peut pas etre remis disponible");
        }
        if (equipment.getActiveAssignment() != null && targetStatus == EquipmentStatus.RETIRED) {
            throw new BusinessException("Retirez d'abord l'affectation active avant de mettre l'equipement au rebut");
        }
    }

    private Equipment getEquipmentEntity(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipement introuvable"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable"));
    }

    private Supplier resolveSupplier(UUID supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
            .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable"));
    }

    private Location resolveLocation(UUID locationId) {
        if (locationId == null) {
            return null;
        }
        return locationRepository.findById(locationId)
            .orElseThrow(() -> new ResourceNotFoundException("Localisation introuvable"));
    }

    private Department resolveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Departement introuvable"));
    }

    private void writeActivity(User actor, String verb, String subjectType, String subjectId, String description, String icon, String color) {
        ActivityLog log = new ActivityLog();
        log.setActor(actor);
        log.setVerb(verb);
        log.setSubjectType(subjectType);
        log.setSubjectId(subjectId);
        log.setDescription(description);
        log.setIcon(icon);
        log.setColor(color);
        activityLogRepository.save(log);
    }
}
