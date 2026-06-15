package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.EquipmentReturn;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRepository extends JpaRepository<EquipmentReturn, UUID> {

    Optional<EquipmentReturn> findByAssignmentId(UUID assignmentId);
}
