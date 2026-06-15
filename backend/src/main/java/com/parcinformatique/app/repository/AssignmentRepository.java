package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.Assignment;
import com.parcinformatique.app.enums.AssignmentStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    long countByStatus(AssignmentStatus status);

    List<Assignment> findByStatus(AssignmentStatus status);

    Page<Assignment> findByStatusOrBeneficiaryEmailContainingIgnoreCase(
        AssignmentStatus status,
        String beneficiaryEmail,
        Pageable pageable
    );

    Page<Assignment> findByBeneficiaryId(UUID beneficiaryId, Pageable pageable);

    java.util.Optional<Assignment> findByIdAndBeneficiaryId(UUID id, UUID beneficiaryId);

    boolean existsByEquipmentIdAndStatusIn(UUID equipmentId, Collection<AssignmentStatus> statuses);
}
