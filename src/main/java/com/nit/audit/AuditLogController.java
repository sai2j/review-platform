package com.nit.audit;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {

        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AuditLog> getAllLogs() {

        return auditLogService.getAllLogs();
    }
}