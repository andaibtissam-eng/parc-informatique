package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.MaterialRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, UUID> {

    @EntityGraph(attributePaths = {"requester", "reviewedBy"})
    List<MaterialRequest> findByRequesterIdOrderByCreatedAtDesc(UUID requesterId);

    @EntityGraph(attributePaths = {"requester", "reviewedBy"})
    List<MaterialRequest> findAllByOrderByCreatedAtDesc();
}
