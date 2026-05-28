package com.infotact.fleet.service;

import com.infotact.fleet.domain.AuditLog;
import com.infotact.fleet.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String performedBy, String details) {
        String actor = performedBy;
        if (actor == null || actor.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                actor = auth.getName();
            } else {
                actor = "SYSTEM";
            }
        }
        try {
            AuditLog auditLog = new AuditLog(action, actor, details);
            auditLogRepository.save(auditLog);
            log.info("AUDIT LOG: [Action: {} | PerformedBy: {} | Details: {}]", action, actor, details);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String details) {
        log(action, null, details);
    }
}
