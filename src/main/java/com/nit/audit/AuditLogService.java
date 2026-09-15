package com.nit.audit;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void log(
            String action,
            String entityType,
            Long entityId,
            String details) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return;
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return;
        }

        AuditLog auditLog = new AuditLog(
                action,
                entityType,
                entityId,
                user.getId(),
                details
        );

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc();
    }
}