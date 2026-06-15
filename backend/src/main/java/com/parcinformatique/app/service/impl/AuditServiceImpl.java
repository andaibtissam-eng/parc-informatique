package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.entity.AuditLog;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.repository.AuditLogRepository;
import com.parcinformatique.app.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(User actor, String action, String entityName, String entityId, String details, String ipAddress, String userAgent) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityIdValue(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        auditLogRepository.save(log);
    }
}
