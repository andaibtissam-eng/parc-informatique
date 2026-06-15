package com.parcinformatique.app.service;

import com.parcinformatique.app.entity.User;

public interface AuditService {

    void log(User actor, String action, String entityName, String entityId, String details, String ipAddress, String userAgent);
}
