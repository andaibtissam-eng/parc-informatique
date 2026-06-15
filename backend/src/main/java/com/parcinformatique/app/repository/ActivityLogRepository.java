package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.ActivityLog;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findTop12ByOrderByOccurredAtDesc();

    List<ActivityLog> findTop8BySubjectTypeInOrderByOccurredAtDesc(Collection<String> subjectTypes);
}
