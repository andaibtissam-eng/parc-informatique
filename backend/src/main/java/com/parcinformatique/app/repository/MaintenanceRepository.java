package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.Maintenance;
import com.parcinformatique.app.enums.MaintenanceStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {

    long countByStatus(MaintenanceStatus status);

    List<Maintenance> findTop8ByOrderByOpenedAtDesc();

    List<Maintenance> findAllByOrderByOpenedAtDesc();

    boolean existsByEquipmentIdAndStatusIn(UUID equipmentId, Collection<MaintenanceStatus> statuses);
}
