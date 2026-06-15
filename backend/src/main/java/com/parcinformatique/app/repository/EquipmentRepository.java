package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.enums.EquipmentStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    long countByStatus(EquipmentStatus status);

    Page<Equipment> findByNameContainingIgnoreCaseOrInventoryCodeContainingIgnoreCase(
        String name,
        String inventoryCode,
        Pageable pageable
    );

    Page<Equipment> findByNameContainingIgnoreCaseOrInventoryCodeContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
        String name,
        String inventoryCode,
        String brand,
        String model,
        Pageable pageable
    );

    List<Equipment> findAllByOrderByNameAsc();

    List<Equipment> findByStatusInOrderByNameAsc(Collection<EquipmentStatus> statuses);
}
